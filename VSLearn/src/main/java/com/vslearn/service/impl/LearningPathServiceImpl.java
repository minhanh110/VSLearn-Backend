package com.vslearn.service.impl;

import com.vslearn.dto.response.LearningPathDTO;
import com.vslearn.dto.response.LessonDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.Topic;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.User;
import com.vslearn.entities.Transaction;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.TransactionRepository;
import com.vslearn.repository.VocabAreaRepository;
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
    private final JwtUtil jwtUtil;

    @Autowired
    public LearningPathServiceImpl(TopicRepository topicRepository, SubTopicRepository subTopicRepository, UserRepository userRepository, TransactionRepository transactionRepository, VocabAreaRepository vocabAreaRepository, JwtUtil jwtUtil) {
        this.topicRepository = topicRepository;
        this.subTopicRepository = subTopicRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.vocabAreaRepository = vocabAreaRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<?> getLearningPath(String authHeader) {
        try {
            List<Topic> allTopics = topicRepository.findAll();
            if (allTopics.isEmpty()) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No topics found in database")
                        .data(new ArrayList<>())
                        .build());
            }
            int maxAllowedTopics = getMaxAllowedTopics(authHeader);
            List<LearningPathDTO> result = new ArrayList<>();
            for (int i = 0; i < allTopics.size(); i++) {
                Topic topic = allTopics.get(i);
                LearningPathDTO dto = new LearningPathDTO();
                dto.setUnitId(topic.getId());
                dto.setTitle(topic.getTopicName());
                dto.setDescription("Học " + topic.getTopicName().toLowerCase());
                boolean isAccessible = (i < maxAllowedTopics);
                dto.setAccessible(isAccessible);
                if (!isAccessible) {
                    if (maxAllowedTopics == 1) {
                        dto.setLockReason("Đăng nhập để mở khóa chủ đề này");
                    } else if (maxAllowedTopics == 2) {
                        dto.setLockReason("Nâng cấp gói học để mở khóa chủ đề này");
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
                    l.setAccessible(isAccessible);
                    return l;
                }).collect(Collectors.toList());
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
} 