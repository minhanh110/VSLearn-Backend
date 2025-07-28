package com.vslearn.service.impl;

import com.vslearn.dto.response.AdminDashboardResponse;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.service.AdminService;
import com.vslearn.entities.User;
import com.vslearn.constant.UserRoles;
import com.vslearn.dto.request.UserManagementRequest;
import com.vslearn.dto.response.ResponseData;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final VocabRepository vocabRepository;
    private final ProgressRepository progressRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, TopicRepository topicRepository, 
                          VocabRepository vocabRepository, ProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.vocabRepository = vocabRepository;
        this.progressRepository = progressRepository;
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalTopics = topicRepository.count();
        long totalVocab = vocabRepository.count();
        long totalProgress = progressRepository.count();
        
        // TODO: Implement more detailed stats
        long activeUsers = totalUsers; // For now, assume all users are active
        long completedLessons = totalProgress; // For now, assume all progress is completed lessons
        long totalTests = 0; // TODO: Implement test counting
        long completedTests = 0; // TODO: Implement completed test counting
        
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalTopics(totalTopics)
                .totalVocab(totalVocab)
                .totalProgress(totalProgress)
                .completedLessons(completedLessons)
                .totalTests(totalTests)
                .completedTests(completedTests)
                .build();
    }

    @Override
    public Map<String, Object> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = totalUsers; // TODO: Implement active user counting
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("newUsersThisMonth", 0); // TODO: Implement
        stats.put("newUsersThisWeek", 0); // TODO: Implement
        
        return stats;
    }

    @Override
    public Map<String, Object> getContentStats() {
        long totalTopics = topicRepository.count();
        long totalVocab = vocabRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTopics", totalTopics);
        stats.put("totalVocab", totalVocab);
        stats.put("activeTopics", totalTopics); // TODO: Implement active topic counting
        stats.put("activeVocab", totalVocab); // TODO: Implement active vocab counting
        
        return stats;
    }

    @Override
    public Map<String, Object> getLearningStats() {
        long totalProgress = progressRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProgress", totalProgress);
        stats.put("completedLessons", totalProgress); // TODO: Implement completed lesson counting
        stats.put("averageCompletionRate", 0.0); // TODO: Implement
        stats.put("mostPopularTopic", "N/A"); // TODO: Implement
        
        return stats;
    }

    // ==================== USER MANAGEMENT ====================
    
    @Override
    public Map<String, Object> getLearnersList(int page, int size, String search, Boolean isActive) {
        try {
            // Get users with LEARNER role
            List<User> allUsers = userRepository.findAll();
            List<User> learners = allUsers.stream()
                .filter(user -> UserRoles.LEARNER.equals(user.getUserRole()))
                .collect(java.util.stream.Collectors.toList());
            
            // Filter by search term
            if (search != null && !search.trim().isEmpty()) {
                learners = learners.stream()
                    .filter(user -> (user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(search.toLowerCase()) ||
                                  user.getUserEmail().toLowerCase().contains(search.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Filter by active status
            if (isActive != null) {
                learners = learners.stream()
                    .filter(user -> user.getIsActive() == isActive)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Calculate pagination
            int totalElements = learners.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<User> paginatedLearners = learners.subList(startIndex, endIndex);
            
            // Convert to DTO format
            List<Map<String, Object>> learnersData = paginatedLearners.stream()
                .map(user -> {
                    Map<String, Object> learnerData = new HashMap<>();
                    learnerData.put("id", user.getId());
                    learnerData.put("name", user.getFirstName() + " " + user.getLastName());
                    learnerData.put("email", user.getUserEmail());
                    learnerData.put("phone", user.getPhoneNumber());
                    learnerData.put("status", user.getIsActive() ? "active" : "inactive");
                    learnerData.put("joinDate", user.getCreatedAt().toString());
                    learnerData.put("topicsCompleted", 0); // TODO: Calculate from progress
                    learnerData.put("packagesOwned", 0); // TODO: Calculate from subscriptions
                    return learnerData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", learnersData);
            result.put("totalElements", (long) totalElements);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalUsers", (long) totalElements);
            result.put("activeUsers", (long) learners.stream().filter(user -> user.getIsActive()).count());
            result.put("inactiveUsers", (long) learners.stream().filter(user -> !user.getIsActive()).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("content", java.util.List.of());
            result.put("totalElements", 0L);
            result.put("totalPages", 0);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    @Override
    public Map<String, Object> getLearnersStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLearners", 0L);
        stats.put("activeLearners", 0L);
        stats.put("inactiveLearners", 0L);
        stats.put("averageTopicsCompleted", 0.0);
        stats.put("averagePackagesOwned", 0.0);
        return stats;
    }
    
    @Override
    public Map<String, Object> getCreatorsList(int page, int size, String search, Boolean isActive) {
        try {
            // Get users with CONTENT_CREATOR role
            List<User> allUsers = userRepository.findAll();
            List<User> creators = allUsers.stream()
                .filter(user -> UserRoles.CONTENT_CREATOR.equals(user.getUserRole()))
                .collect(java.util.stream.Collectors.toList());
            
            // Filter by search term
            if (search != null && !search.trim().isEmpty()) {
                creators = creators.stream()
                    .filter(user -> (user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(search.toLowerCase()) ||
                                  user.getUserEmail().toLowerCase().contains(search.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Filter by active status
            if (isActive != null) {
                creators = creators.stream()
                    .filter(user -> user.getIsActive() == isActive)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Calculate pagination
            int totalElements = creators.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<User> paginatedCreators = creators.subList(startIndex, endIndex);
            
            // Convert to DTO format
            List<Map<String, Object>> creatorsData = paginatedCreators.stream()
                .map(user -> {
                    Map<String, Object> creatorData = new HashMap<>();
                    creatorData.put("id", user.getId());
                    creatorData.put("name", user.getFirstName() + " " + user.getLastName());
                    creatorData.put("email", user.getUserEmail());
                    creatorData.put("phone", user.getPhoneNumber());
                    creatorData.put("status", user.getIsActive() ? "active" : "inactive");
                    creatorData.put("joinDate", user.getCreatedAt().toString());
                    creatorData.put("topicsCreated", 0); // TODO: Calculate from topics
                    creatorData.put("vocabularyCreated", 0); // TODO: Calculate from vocab
                    creatorData.put("pendingApproval", 0); // TODO: Calculate from pending topics
                    creatorData.put("specialization", "N/A"); // TODO: Add specialization field
                    creatorData.put("avatar", user.getUserAvatar());
                    return creatorData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", creatorsData);
            result.put("totalElements", (long) totalElements);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalUsers", (long) totalElements);
            result.put("activeUsers", (long) creators.stream().filter(user -> user.getIsActive()).count());
            result.put("inactiveUsers", (long) creators.stream().filter(user -> !user.getIsActive()).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("content", java.util.List.of());
            result.put("totalElements", 0L);
            result.put("totalPages", 0);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    @Override
    public Map<String, Object> getCreatorsStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCreators", 0L);
        stats.put("activeCreators", 0L);
        stats.put("inactiveCreators", 0L);
        stats.put("totalTopicsCreated", 0L);
        stats.put("totalVocabularyCreated", 0L);
        stats.put("pendingApproval", 0L);
        return stats;
    }
    
    @Override
    public Map<String, Object> getApproversList(int page, int size, String search, Boolean isActive) {
        try {
            // Get users with CONTENT_APPROVER role
            List<User> allUsers = userRepository.findAll();
            List<User> approvers = allUsers.stream()
                .filter(user -> UserRoles.CONTENT_APPROVER.equals(user.getUserRole()))
                .collect(java.util.stream.Collectors.toList());
            
            // Filter by search term
            if (search != null && !search.trim().isEmpty()) {
                approvers = approvers.stream()
                    .filter(user -> (user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(search.toLowerCase()) ||
                                  user.getUserEmail().toLowerCase().contains(search.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Filter by active status
            if (isActive != null) {
                approvers = approvers.stream()
                    .filter(user -> user.getIsActive() == isActive)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Calculate pagination
            int totalElements = approvers.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<User> paginatedApprovers = approvers.subList(startIndex, endIndex);
            
            // Convert to DTO format
            List<Map<String, Object>> approversData = paginatedApprovers.stream()
                .map(user -> {
                    Map<String, Object> approverData = new HashMap<>();
                    approverData.put("id", user.getId());
                    approverData.put("name", user.getFirstName() + " " + user.getLastName());
                    approverData.put("email", user.getUserEmail());
                    approverData.put("phone", user.getPhoneNumber());
                    approverData.put("status", user.getIsActive() ? "active" : "inactive");
                    approverData.put("joinDate", user.getCreatedAt().toString());
                    approverData.put("topicsApproved", 0); // TODO: Calculate from approved topics
                    approverData.put("pendingReview", 0); // TODO: Calculate from pending topics
                    approverData.put("specialization", "N/A"); // TODO: Add specialization field
                    approverData.put("avatar", user.getUserAvatar());
                    return approverData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", approversData);
            result.put("totalElements", (long) totalElements);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalUsers", (long) totalElements);
            result.put("activeUsers", (long) approvers.stream().filter(user -> user.getIsActive()).count());
            result.put("inactiveUsers", (long) approvers.stream().filter(user -> !user.getIsActive()).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("content", java.util.List.of());
            result.put("totalElements", 0L);
            result.put("totalPages", 0);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    @Override
    public Map<String, Object> getApproversStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApprovers", 0L);
        stats.put("activeApprovers", 0L);
        stats.put("inactiveApprovers", 0L);
        stats.put("totalTopicsApproved", 0L);
        stats.put("pendingReview", 0L);
        return stats;
    }
    
    @Override
    public Map<String, Object> getAllUsersList(int page, int size, String search, String userRole, Boolean isActive) {
        // TODO: Implement with proper filtering and pagination
        Map<String, Object> result = new HashMap<>();
        result.put("users", java.util.List.of()); // Empty list for now
        result.put("totalElements", 0L);
        result.put("totalPages", 0);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("totalUsers", 0L);
        result.put("activeUsers", 0L);
        result.put("inactiveUsers", 0L);
        result.put("learnersCount", 0L);
        result.put("creatorsCount", 0L);
        result.put("approversCount", 0L);
        result.put("managersCount", 0L);
        return result;
    }
    
    @Override
    public Map<String, Object> getUserById(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("name", user.getFirstName() + " " + user.getLastName());
            userData.put("userName", user.getUserName());
            userData.put("email", user.getUserEmail());
            userData.put("phone", user.getPhoneNumber());
            userData.put("userRole", user.getUserRole());
            userData.put("status", user.getIsActive() ? "active" : "inactive");
            userData.put("isActive", user.getIsActive());
            userData.put("avatar", user.getUserAvatar());
            userData.put("provider", user.getProvider());
            userData.put("joinDate", user.getCreatedAt().toString());
            userData.put("lastLogin", user.getModifyTime() != null ? user.getModifyTime().toString() : "N/A");
            
            // Role-specific data
            switch (user.getUserRole()) {
                case UserRoles.LEARNER:
                    userData.put("topicsCompleted", 0); // TODO: Calculate from progress
                    userData.put("packagesOwned", 0); // TODO: Calculate from subscriptions
                    userData.put("address", "N/A"); // TODO: Add address field
                    userData.put("birthDate", "N/A"); // TODO: Add birth date field
                    break;
                case UserRoles.CONTENT_CREATOR:
                    userData.put("topicsCreated", 0); // TODO: Calculate from topics
                    userData.put("vocabularyCreated", 0); // TODO: Calculate from vocab
                    userData.put("pendingApproval", 0); // TODO: Calculate from pending topics
                    userData.put("approvedContent", 0); // TODO: Calculate from approved topics
                    userData.put("rejectedContent", 0); // TODO: Calculate from rejected topics
                    userData.put("specialization", "N/A"); // TODO: Add specialization field
                    userData.put("bio", "N/A"); // TODO: Add bio field
                    break;
                case UserRoles.CONTENT_APPROVER:
                    userData.put("topicsApproved", 0); // TODO: Calculate from approved topics
                    userData.put("pendingReview", 0); // TODO: Calculate from pending topics
                    userData.put("rejectedTopics", 0); // TODO: Calculate from rejected topics
                    userData.put("totalReviewed", 0); // TODO: Calculate total reviewed
                    userData.put("specialization", "N/A"); // TODO: Add specialization field
                    userData.put("bio", "N/A"); // TODO: Add bio field
                    break;
                default:
                    break;
            }
            
            return userData;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public ResponseEntity<?> createUserByManager(UserManagementRequest req) {
        // Kiểm tra trùng username/email/phone nếu cần
        if (userRepository.existsByUserName(req.getUserName())) {
            return ResponseEntity.badRequest().body(
                ResponseData.builder().status(400).message("Tên đăng nhập đã tồn tại").build()
            );
        }
        if (userRepository.existsByUserEmail(req.getUserEmail())) {
            return ResponseEntity.badRequest().body(
                ResponseData.builder().status(400).message("Email đã tồn tại").build()
            );
        }
        // ... kiểm tra phone nếu muốn ...

        User user = User.builder()
            .userName(req.getUserName())
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .userEmail(req.getUserEmail())
            .phoneNumber(req.getPhoneNumber())
            .userRole("ROLE_" + req.getUserRole()) // Ví dụ: ROLE_LEARNER
            .userAvatar(req.getUserAvatar())
            .isActive(req.getIsActive() != null ? req.getIsActive() : true)
            .userPassword(passwordEncoder.encode("123456")) // hoặc cho phép nhập password
            .createdAt(java.time.Instant.now())
            .build();

        try {
            userRepository.save(user);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Duplicate entry") && msg.contains("phone_number")) {
                return ResponseEntity.badRequest().body(ResponseData.builder().status(400).message("Số điện thoại đã tồn tại").build());
            }
            if (msg != null && msg.contains("Duplicate entry") && msg.contains("user_email")) {
                return ResponseEntity.badRequest().body(ResponseData.builder().status(400).message("Email đã tồn tại").build());
            }
            if (msg != null && msg.contains("Duplicate entry") && msg.contains("user_name")) {
                return ResponseEntity.badRequest().body(ResponseData.builder().status(400).message("Tên đăng nhập đã tồn tại").build());
            }
            return ResponseEntity.badRequest().body(ResponseData.builder().status(400).message("Tạo user thất bại: " + e.getMessage()).build());
        }

        return ResponseEntity.ok(
            ResponseData.builder().status(200).message("Tạo user thành công").data(user).build()
        );
    }

    @Override
    public ResponseEntity<?> updateUserByManager(Long userId, UserManagementRequest req) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            // Chỉ cập nhật các trường có sẵn trong UserManagementRequest
            if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
            if (req.getLastName() != null) user.setLastName(req.getLastName());
            if (req.getUserName() != null) user.setUserName(req.getUserName());
            if (req.getUserEmail() != null) user.setUserEmail(req.getUserEmail());
            if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
            if (req.getUserAvatar() != null) user.setUserAvatar(req.getUserAvatar());
            if (req.getIsActive() != null) user.setIsActive(req.getIsActive());
            if (req.getUserRole() != null) user.setUserRole(req.getUserRole());
            user.setUpdatedAt(java.time.Instant.now());
            userRepository.save(user);
            return ResponseEntity.ok(ResponseData.builder().status(200).message("Cập nhật user thành công").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseData.builder().status(400).message("Cập nhật user thất bại: " + e.getMessage()).build());
        }
    }
} 