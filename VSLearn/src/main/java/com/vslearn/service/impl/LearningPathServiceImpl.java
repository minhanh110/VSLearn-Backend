package com.vslearn.service.impl;

import com.vslearn.dto.response.LearningPathDTO;
import com.vslearn.dto.response.LessonDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.Topic;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.User;
import com.vslearn.entities.Transaction;
import com.vslearn.entities.Progress;
import com.vslearn.entities.TopicPoint;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.TransactionRepository;
import com.vslearn.repository.VocabAreaRepository;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.repository.TopicPointRepository;
import com.vslearn.service.LearningPathService;
import com.vslearn.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningPathServiceImpl implements LearningPathService {
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final VocabAreaRepository vocabAreaRepository;
    private final ProgressRepository progressRepository;
    private final TopicPointRepository topicPointRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public LearningPathServiceImpl(TopicRepository topicRepository, SubTopicRepository subTopicRepository, UserRepository userRepository, TransactionRepository transactionRepository, VocabAreaRepository vocabAreaRepository, ProgressRepository progressRepository, TopicPointRepository topicPointRepository, JwtUtil jwtUtil) {
        this.topicRepository = topicRepository;
        this.subTopicRepository = subTopicRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.vocabAreaRepository = vocabAreaRepository;
        this.progressRepository = progressRepository;
        this.topicPointRepository = topicPointRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<?> getLearningPath(String authHeader) {
        try {
            // Show active and request_update topics ordered by sortOrder on homepage; only parent records
            List<String> homepageStatuses = java.util.Arrays.asList("active", "request_update");
            List<Topic> allTopics = topicRepository.findByParentIsNullAndStatusInAndDeletedAtIsNullOrderBySortOrderAsc(homepageStatuses);
            if (allTopics.isEmpty()) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No topics found in database")
                        .data(new ArrayList<>())
                        .build());
            }
            
            // Get user info and max allowed topics
            UserInfo userInfo = getUserInfo(authHeader);
            int maxAllowedTopics = userInfo.maxAllowedTopics;
            
            List<LearningPathDTO> result = new ArrayList<>();
            for (int i = 0; i < allTopics.size(); i++) {
                Topic topic = allTopics.get(i);
                final int topicIndex = i; // Create final variable for lambda
                LearningPathDTO dto = new LearningPathDTO();
                dto.setUnitId(topic.getId());
                dto.setTitle(topic.getTopicName());
                dto.setDescription("Học " + topic.getTopicName().toLowerCase());
                
                // Check if topic is accessible based on user type and previous topic completion
                boolean isTopicAccessible = checkTopicAccessibility(topic, topicIndex, userInfo);
                dto.setAccessible(isTopicAccessible);
                
                if (!isTopicAccessible) {
                    if (maxAllowedTopics == 1) {
                        dto.setLockReason("Đăng nhập để mở khóa chủ đề này");
                    } else if (maxAllowedTopics == 2 && topicIndex >= 2) {
                        dto.setLockReason("Nâng cấp gói học để mở khóa chủ đề này");
                    } else if (topicIndex > 0) {
                        dto.setLockReason("Hoàn thành bài test của chủ đề trước với điểm ≥90%");
                    }
                }
                
                List<SubTopic> subTopics = subTopicRepository.findByTopic_Id(topic.getId());
                List<LessonDTO> lessons = subTopics.stream().map(sub -> {
                    LessonDTO l = new LessonDTO();
                    l.setId(sub.getId());
                    l.setTitle(sub.getSubTopicName());
                    l.setIsTest(sub.getStatus() != null && sub.getStatus().toLowerCase().contains("test"));
                    if (l.getIsTest()) {
                        l.setQuestionCount(10);
                        l.setWordCount(null);
                    } else {
                        long actualWordCount = vocabAreaRepository.countByVocabSubTopicId(sub.getId());
                        l.setWordCount((int) actualWordCount);
                        l.setQuestionCount(null);
                    }
                    
                    // Check if lesson is accessible based on progress
                    boolean isLessonAccessible = checkLessonAccessibility(sub, topic, topicIndex, userInfo);
                    l.setAccessible(isTopicAccessible && isLessonAccessible);
                    
                    // Check if lesson is completed
                    boolean isLessonCompleted = userInfo.completedSubtopicIds.contains(sub.getId());
                    l.setIsCompleted(isLessonCompleted);
                    
                    return l;
                }).collect(Collectors.toList());
                
                // Add test lesson for each topic (since test questions are generated from topic vocab)
                LessonDTO testLesson = new LessonDTO();
                testLesson.setId(topic.getId() + 1000); // Use topic ID + 1000 as test lesson ID
                testLesson.setTitle("Bài test - " + topic.getTopicName());
                testLesson.setIsTest(true);
                testLesson.setQuestionCount(20);
                testLesson.setWordCount(null);
                
                // Test is accessible if all subtopics are completed
                List<SubTopic> nonTestSubtopics = subTopics.stream()
                    .filter(st -> st.getStatus() == null || !st.getStatus().toLowerCase().contains("test"))
                    .collect(Collectors.toList());
                boolean isTestAccessible = nonTestSubtopics.stream()
                    .allMatch(st -> userInfo.completedSubtopicIds.contains(st.getId()));
                testLesson.setAccessible(isTopicAccessible && isTestAccessible);
                

                
                lessons.add(testLesson);
                dto.setLessons(lessons);
                result.add(dto);
            }
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Success")
                    .data(result)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .data(new ArrayList<>())
                    .build());
        }
    }

    private UserInfo getUserInfo(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new UserInfo(null, 1, new ArrayList<>(), new ArrayList<>());
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = (String) jwtUtil.getClaimsFromToken(token).getClaims().get("id");
            if (userId == null) {
                return new UserInfo(null, 1, new ArrayList<>(), new ArrayList<>());
            }
            User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
            if (user == null) {
                return new UserInfo(null, 1, new ArrayList<>(), new ArrayList<>());
            }
            
            // Get user progress
            List<Progress> userProgress = progressRepository.findByCreatedBy_Id(user.getId());
            List<Long> completedSubtopicIds = userProgress.stream()
                .filter(Progress::getIsComplete)
                .map(p -> p.getSubTopic().getId())
                .collect(Collectors.toList());
            
            // Get user test results
            List<TopicPoint> userTopicPoints = topicPointRepository.findByCreatedById(user.getId());
            List<TopicTestResult> testResults = userTopicPoints.stream()
                .map(tp -> new TopicTestResult(tp.getTopic().getId(), tp.getTotalPoint()))
                .collect(Collectors.toList());
            
            boolean hasValidSubscription = checkValidSubscription(user.getId());
            int maxAllowedTopics = hasValidSubscription ? Integer.MAX_VALUE : 2;
            
            return new UserInfo(user.getId(), maxAllowedTopics, completedSubtopicIds, testResults);
        } catch (Exception e) {
            return new UserInfo(null, 1, new ArrayList<>(), new ArrayList<>());
        }
    }

    private boolean checkTopicAccessibility(Topic topic, int topicIndex, UserInfo userInfo) {
        // Check if topic is within allowed range for user type
        if (topicIndex >= userInfo.maxAllowedTopics) {
            return false;
        }
        
        // First topic is always accessible
        if (topicIndex == 0) {
            return true;
        }
        
        // For subsequent topics, check if previous topic test was passed with ≥90%
        Topic previousTopic = topicRepository.findAll().stream()
            .sorted(Comparator.comparing(Topic::getId))
            .collect(Collectors.toList())
            .get(topicIndex - 1);
        
        return userInfo.testResults.stream()
            .anyMatch(tr -> tr.topicId.equals(previousTopic.getId()) && tr.score >= 90.0);
    }

    private boolean checkLessonAccessibility(SubTopic subtopic, Topic topic, int topicIndex, UserInfo userInfo) {
        // If it's a test, check if all subtopics in the topic are completed
        if (subtopic.getStatus() != null && subtopic.getStatus().toLowerCase().contains("test")) {
            List<SubTopic> allSubtopicsInTopic = subTopicRepository.findByTopic_Id(topic.getId());
            List<SubTopic> nonTestSubtopics = allSubtopicsInTopic.stream()
                .filter(st -> st.getStatus() == null || !st.getStatus().toLowerCase().contains("test"))
                .collect(Collectors.toList());
            
            // All non-test subtopics must be completed
            return nonTestSubtopics.stream()
                .allMatch(st -> userInfo.completedSubtopicIds.contains(st.getId()));
        }
        
        // For regular subtopics, check if previous subtopic is completed
        List<SubTopic> allSubtopicsInTopic = subTopicRepository.findByTopic_Id(topic.getId());
        List<SubTopic> nonTestSubtopics = allSubtopicsInTopic.stream()
            .filter(st -> st.getStatus() == null || !st.getStatus().toLowerCase().contains("test"))
            .sorted(Comparator.comparing(SubTopic::getId))
            .collect(Collectors.toList());
        
        // Find current subtopic index
        int currentIndex = -1;
        for (int i = 0; i < nonTestSubtopics.size(); i++) {
            if (nonTestSubtopics.get(i).getId().equals(subtopic.getId())) {
                currentIndex = i;
                break;
            }
        }
        
        // First subtopic is always accessible
        if (currentIndex == 0) {
            return true;
        }
        
        // Check if previous subtopic is completed
        if (currentIndex > 0) {
            SubTopic previousSubtopic = nonTestSubtopics.get(currentIndex - 1);
            return userInfo.completedSubtopicIds.contains(previousSubtopic.getId());
        }
        
        return false;
    }

    private int getMaxAllowedTopics(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 1;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = (String) jwtUtil.getClaimsFromToken(token).getClaims().get("id");
            if (userId == null) {
                return 1;
            }
            User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
            if (user == null) {
                return 1;
            }
            boolean hasValidSubscription = checkValidSubscription(user.getId());
            if (hasValidSubscription) {
                return Integer.MAX_VALUE;
            } else {
                return 2;
            }
        } catch (Exception e) {
            return 1;
        }
    }

    private boolean checkValidSubscription(Long userId) {
        try {
            List<Transaction> userTransactions = transactionRepository.findByCreatedBy_Id(userId);
            if (userTransactions.isEmpty()) {
                return false;
            }
            Transaction latestTransaction = userTransactions.stream()
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .findFirst()
                    .orElse(null);
            if (latestTransaction == null) {
                return false;
            }
            Instant now = Instant.now();
            return now.isAfter(latestTransaction.getStartDate()) && now.isBefore(latestTransaction.getEndDate());
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper classes
    private static class UserInfo {
        final Long userId;
        final int maxAllowedTopics;
        final List<Long> completedSubtopicIds;
        final List<TopicTestResult> testResults;
        
        UserInfo(Long userId, int maxAllowedTopics, List<Long> completedSubtopicIds, List<TopicTestResult> testResults) {
            this.userId = userId;
            this.maxAllowedTopics = maxAllowedTopics;
            this.completedSubtopicIds = completedSubtopicIds;
            this.testResults = testResults;
        }
    }
    
    private static class TopicTestResult {
        final Long topicId;
        final Double score;
        
        TopicTestResult(Long topicId, Double score) {
            this.topicId = topicId;
            this.score = score;
        }
    }
} 