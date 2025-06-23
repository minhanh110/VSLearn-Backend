package com.vslearn.controller;

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
import com.vslearn.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/learning-path")
@CrossOrigin(origins = "http://localhost:3000")
public class LearningPathController {

    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private SubTopicRepository subTopicRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private VocabAreaRepository vocabAreaRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getLearningPath(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Log để debug
            System.out.println("=== LearningPathController.getLearningPath() called ===");
            
            List<Topic> allTopics = topicRepository.findAll();
            System.out.println("Found " + allTopics.size() + " topics in database");
            
            if (allTopics.isEmpty()) {
                System.out.println("WARNING: No topics found in database!");
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No topics found in database")
                        .data(new ArrayList<>())
                        .build());
            }

            // Xác định quyền truy cập dựa trên authentication
            int maxAllowedTopics = getMaxAllowedTopics(authHeader);
            System.out.println("Max allowed topics: " + maxAllowedTopics);
            
            List<LearningPathDTO> result = new ArrayList<>();

            for (int i = 0; i < allTopics.size(); i++) {
                Topic topic = allTopics.get(i);
                System.out.println("Processing topic: " + topic.getTopicName() + " (ID: " + topic.getId() + ")");
                
                LearningPathDTO dto = new LearningPathDTO();
                dto.setUnitId(topic.getId());
                dto.setTitle(topic.getTopicName());
                
                // Tạo description từ topic name hoặc có thể lấy từ database
                dto.setDescription("Học " + topic.getTopicName().toLowerCase());

                // Xác định xem topic này có được phép truy cập không
                boolean isAccessible = (i < maxAllowedTopics);
                dto.setAccessible(isAccessible);
                
                // Thêm thông tin về lý do bị khóa
                if (!isAccessible) {
                    if (maxAllowedTopics == 1) {
                        dto.setLockReason("Đăng nhập để mở khóa chủ đề này");
                    } else if (maxAllowedTopics == 2) {
                        dto.setLockReason("Nâng cấp gói học để mở khóa chủ đề này");
                    }
                }

                List<SubTopic> subTopics = subTopicRepository.findByTopic_Id(topic.getId());
                System.out.println("Found " + subTopics.size() + " sub-topics for topic " + topic.getId());
                
                List<LessonDTO> lessons = subTopics.stream().map(sub -> {
                    LessonDTO l = new LessonDTO();
                    l.setId(sub.getId());
                    l.setTitle(sub.getSubTopicName());
                    l.setIsTest(sub.getStatus() != null && sub.getStatus().toLowerCase().contains("test"));
                    
                    // Lấy số từ vựng thực tế từ database
                    if (l.getIsTest()) {
                        l.setQuestionCount(10); // Default 10 câu hỏi cho test
                        l.setWordCount(null);
                    } else {
                        // Đếm số từ vựng thực tế của subtopic
                        long actualWordCount = vocabAreaRepository.countByVocabSubTopicId(sub.getId());
                        l.setWordCount((int) actualWordCount);
                        l.setQuestionCount(null);
                    }
                    
                    // Lesson cũng bị khóa nếu topic bị khóa
                    l.setAccessible(isAccessible);
                    
                    System.out.println("  - Lesson: " + l.getTitle() + " (ID: " + l.getId() + ", isTest: " + l.getIsTest() + ", wordCount: " + l.getWordCount() + ", accessible: " + isAccessible + ")");
                    return l;
                }).collect(Collectors.toList());

                dto.setLessons(lessons);
                result.add(dto);
            }

            System.out.println("Returning " + result.size() + " units");
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Success")
                    .data(result)
                    .build());

        } catch (Exception e) {
            System.err.println("Error in getLearningPath: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .data(new ArrayList<>())
                    .build());
        }
    }

    /**
     * Xác định số lượng topic tối đa mà user có thể truy cập
     * @param authHeader Authorization header
     * @return Số lượng topic tối đa được phép
     */
    private int getMaxAllowedTopics(String authHeader) {
        // Không có token = không đăng nhập
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No authentication - allowing 1 topic");
            return 1; // Chỉ topic đầu tiên
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = (String) jwtUtil.getClaimsFromToken(token).getClaims().get("id");
            
            if (userId == null) {
                System.out.println("Invalid token - allowing 1 topic");
                return 1;
            }

            User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
            if (user == null) {
                System.out.println("User not found - allowing 1 topic");
                return 1;
            }

            // Kiểm tra xem user có gói học hợp lệ không
            boolean hasValidSubscription = checkValidSubscription(user.getId());
            
            if (hasValidSubscription) {
                System.out.println("User has valid subscription - allowing all topics");
                return Integer.MAX_VALUE; // Tất cả topics
            } else {
                System.out.println("User has no subscription - allowing 2 topics");
                return 2; // 2 topics đầu
            }

        } catch (Exception e) {
            System.err.println("Error checking user permissions: " + e.getMessage());
            return 1; // Fallback về 1 topic nếu có lỗi
        }
    }

    /**
     * Kiểm tra xem user có gói học hợp lệ không
     * @param userId ID của user
     * @return true nếu có gói học hợp lệ
     */
    private boolean checkValidSubscription(Long userId) {
        try {
            // Tìm transaction gần nhất của user
            List<Transaction> userTransactions = transactionRepository.findByCreatedBy_Id(userId);
            
            if (userTransactions.isEmpty()) {
                return false;
            }

            // Sắp xếp theo thời gian tạo, lấy transaction mới nhất
            Transaction latestTransaction = userTransactions.stream()
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .findFirst()
                    .orElse(null);

            if (latestTransaction == null) {
                return false;
            }

            // Kiểm tra xem gói học có còn hiệu lực không
            Instant now = Instant.now();
            return now.isAfter(latestTransaction.getStartDate()) && 
                   now.isBefore(latestTransaction.getEndDate());

        } catch (Exception e) {
            System.err.println("Error checking subscription: " + e.getMessage());
            return false;
        }
    }
} 