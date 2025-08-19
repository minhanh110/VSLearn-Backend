package com.vslearn.service.impl;

import com.vslearn.dto.response.AdminDashboardResponse;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.repository.TransactionRepository;
import com.vslearn.service.AdminService;
import com.vslearn.entities.User;
import com.vslearn.entities.Transaction;
import com.vslearn.entities.Progress;
import com.vslearn.entities.Topic;
import com.vslearn.entities.Vocab;
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
import java.util.Optional;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import com.vslearn.entities.Transaction.PaymentStatus;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final VocabRepository vocabRepository;
    private final ProgressRepository progressRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, TopicRepository topicRepository, 
                          VocabRepository vocabRepository, ProgressRepository progressRepository,
                          TransactionRepository transactionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.vocabRepository = vocabRepository;
        this.progressRepository = progressRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
                    // Calculate topics completed from progress
                    long topicsCompleted = progressRepository.countByCreatedBy_IdAndIsComplete(user.getId(), true);
                    
                    // Calculate packages owned from transactions
                    long packagesOwned = transactionRepository.countByCreatedBy_IdAndPaymentStatus(user.getId(), PaymentStatus.PAID);
                    
                    Map<String, Object> learnerData = new HashMap<>();
                    learnerData.put("id", user.getId());
                    learnerData.put("name", user.getFirstName() + " " + user.getLastName());
                    learnerData.put("email", user.getUserEmail());
                    learnerData.put("phone", user.getPhoneNumber());
                    learnerData.put("status", user.getIsActive() ? "active" : "inactive");
                    learnerData.put("joinDate", user.getCreatedAt().toString());
                    learnerData.put("topicsCompleted", topicsCompleted);
                    learnerData.put("packagesOwned", packagesOwned);
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
                    // Calculate topics created by this creator
                    long topicsCreated = topicRepository.findAll().stream()
                        .filter(topic -> topic.getCreatedBy().equals(user.getId()))
                        .count();
                    
                    // Calculate vocabulary created by this creator
                    long vocabularyCreated = vocabRepository.findAll().stream()
                        .filter(vocab -> vocab.getCreatedBy().equals(user.getId()))
                        .count();
                    
                    // Calculate pending approval topics (topics with status not "APPROVED")
                    long pendingApproval = topicRepository.findAll().stream()
                        .filter(topic -> topic.getCreatedBy().equals(user.getId()))
                        .filter(topic -> !"APPROVED".equals(topic.getStatus()))
                        .count();
                    
                    Map<String, Object> creatorData = new HashMap<>();
                    creatorData.put("id", user.getId());
                    creatorData.put("name", user.getFirstName() + " " + user.getLastName());
                    creatorData.put("email", user.getUserEmail());
                    creatorData.put("phone", user.getPhoneNumber());
                    creatorData.put("status", user.getIsActive() ? "active" : "inactive");
                    creatorData.put("joinDate", user.getCreatedAt().toString());
                    creatorData.put("topicsCreated", topicsCreated);
                    creatorData.put("vocabularyCreated", vocabularyCreated);
                    creatorData.put("pendingApproval", pendingApproval);
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
                    // Calculate topics approved by this approver
                    long topicsApproved = topicRepository.findAll().stream()
                        .filter(topic -> topic.getUpdatedBy() != null && topic.getUpdatedBy().equals(user.getId()))
                        .filter(topic -> "APPROVED".equals(topic.getStatus()))
                        .count();
                    
                    // Calculate pending review topics (topics with status not "APPROVED" and not "REJECTED")
                    long pendingReview = topicRepository.findAll().stream()
                        .filter(topic -> !"APPROVED".equals(topic.getStatus()) && !"REJECTED".equals(topic.getStatus()))
                        .count();
                    
                    Map<String, Object> approverData = new HashMap<>();
                    approverData.put("id", user.getId());
                    approverData.put("name", user.getFirstName() + " " + user.getLastName());
                    approverData.put("email", user.getUserEmail());
                    approverData.put("phone", user.getPhoneNumber());
                    approverData.put("status", user.getIsActive() ? "active" : "inactive");
                    approverData.put("joinDate", user.getCreatedAt().toString());
                    approverData.put("topicsApproved", topicsApproved);
                    approverData.put("pendingReview", pendingReview);
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
            userData.put("joinDate", user.getCreatedAt().toString().substring(0, 10));
            userData.put("lastLogin", user.getModifyTime() != null ? user.getModifyTime().toString().substring(0, 19).replace("T", " ") : "N/A");
            
            // Role-specific data
            switch (user.getUserRole()) {
                case UserRoles.LEARNER:
                    // Calculate topics completed from progress
                    List<Progress> userProgress = progressRepository.findByCreatedBy_Id(userId);
                    long completedTopics = userProgress.stream().filter(Progress::getIsComplete).count();
                    userData.put("topicsCompleted", completedTopics);
                    
                    // Calculate packages owned from transactions
                    List<Transaction> userTransactions = transactionRepository.findByCreatedBy_Id(userId);
                    userData.put("packagesOwned", userTransactions.size());
                    
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
            .phoneNumber(req.getPhoneNumber() != null && !req.getPhoneNumber().equals("N/A") ? req.getPhoneNumber() : null)
            .userRole(req.getUserRole() != null && !req.getUserRole().startsWith("ROLE_") ? "ROLE_" + req.getUserRole() : req.getUserRole()) // Đảm bảo có prefix ROLE_
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
            if (req.getPhoneNumber() != null && !req.getPhoneNumber().equals("N/A")) {
                user.setPhoneNumber(req.getPhoneNumber());
            } else {
                user.setPhoneNumber(null);
            }
            if (req.getUserAvatar() != null) user.setUserAvatar(req.getUserAvatar());
            if (req.getIsActive() != null) user.setIsActive(req.getIsActive());
            if (req.getUserRole() != null) {
                String userRole = req.getUserRole();
                // Đảm bảo role có prefix ROLE_
                if (!userRole.startsWith("ROLE_")) {
                    userRole = "ROLE_" + userRole;
                }
                user.setUserRole(userRole);
            }
            user.setUpdatedAt(java.time.Instant.now());
            userRepository.save(user);
            return ResponseEntity.ok(ResponseData.builder().status(200).message("Cập nhật user thành công").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseData.builder().status(400).message("Cập nhật user thất bại: " + e.getMessage()).build());
        }
    }

    @Override
    public Map<String, Object> getLearnerPackages(Long userId) {
        try {
            List<Transaction> transactions = transactionRepository.findByCreatedBy_Id(userId);
            
            List<Map<String, Object>> packages = transactions.stream()
                .map(transaction -> {
                    Map<String, Object> pkg = new HashMap<>();
                    pkg.put("id", transaction.getId());
                    pkg.put("name", transaction.getPricing().getPricingType());
                    pkg.put("type", transaction.getPricing().getDurationDays() + "-days");
                    pkg.put("status", transaction.getEndDate().isAfter(Instant.now()) ? "active" : "expired");
                    pkg.put("startDate", transaction.getStartDate().toString().substring(0, 10));
                    pkg.put("endDate", transaction.getEndDate().toString().substring(0, 10));
                    pkg.put("price", transaction.getAmount() != null ? transaction.getAmount().toString() : "0");
                    pkg.put("code", transaction.getCode());
                    return pkg;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("packages", packages);
            result.put("totalPackages", packages.size());
            result.put("activePackages", packages.stream().filter(p -> "active".equals(p.get("status"))).count());
            result.put("expiredPackages", packages.stream().filter(p -> "expired".equals(p.get("status"))).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("packages", new java.util.ArrayList<>());
            result.put("totalPackages", 0);
            result.put("activePackages", 0);
            result.put("expiredPackages", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getLearnerActivities(Long userId) {
        try {
            List<Progress> progressList = progressRepository.findByCreatedBy_Id(userId);
            
            List<Map<String, Object>> activities = progressList.stream()
                .map(progress -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", progress.getId());
                    activity.put("action", "Hoàn thành chủ đề: " + progress.getSubTopic().getSubTopicName());
                    activity.put("time", progress.getCreatedAt().toString().substring(0, 19).replace("T", " "));
                    activity.put("type", "topic");
                    activity.put("isComplete", progress.getIsComplete());
                    return activity;
                })
                .sorted((a, b) -> b.get("time").toString().compareTo(a.get("time").toString()))
                .limit(10) // Chỉ lấy 10 hoạt động gần nhất
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("activities", activities);
            result.put("totalActivities", activities.size());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("activities", new java.util.ArrayList<>());
            result.put("totalActivities", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getLearnerDetailedStats(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Progress> progressList = progressRepository.findByCreatedBy_Id(userId);
            List<Transaction> transactions = transactionRepository.findByCreatedBy_Id(userId);
            
            long completedTopics = progressList.stream().filter(Progress::getIsComplete).count();
            long totalPackages = transactions.size();
            long activePackages = transactions.stream()
                .filter(t -> t.getEndDate().isAfter(Instant.now()))
                .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("completedTopics", completedTopics);
            stats.put("totalPackages", totalPackages);
            stats.put("activePackages", activePackages);
            stats.put("totalProgress", progressList.size());
            stats.put("lastActivity", progressList.isEmpty() ? null : 
                progressList.stream()
                    .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .map(p -> p.getCreatedAt().toString().substring(0, 19).replace("T", " "))
                    .orElse(null));
            
            return stats;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("completedTopics", 0);
            result.put("totalPackages", 0);
            result.put("activePackages", 0);
            result.put("totalProgress", 0);
            result.put("lastActivity", null);
            return result;
        }
    }

    @Override
    public Map<String, Object> getCreatorTopics(Long userId) {
        try {
            List<Topic> topics = topicRepository.findByCreatedByAndDeletedAtIsNull(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            List<Map<String, Object>> topicsList = topics.stream()
                .map(topic -> {
                    Map<String, Object> topicData = new HashMap<>();
                    topicData.put("id", topic.getId());
                    topicData.put("name", topic.getTopicName());
                    topicData.put("status", topic.getStatus());
                    topicData.put("createdDate", topic.getCreatedAt().toString().substring(0, 10));
                    topicData.put("isFree", topic.getIsFree());
                    topicData.put("sortOrder", topic.getSortOrder());
                    
                    // Count vocabularies in this topic - simplified for now
                    topicData.put("vocabulary", 0); // TODO: Implement proper counting
                    
                    // Mock approver info for now (TODO: implement approval system)
                    topicData.put("approver", topic.getStatus().equals("APPROVED") ? "System" : null);
                    
                    return topicData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("topics", topicsList);
            result.put("totalTopics", topicsList.size());
            result.put("approvedTopics", topicsList.stream().filter(t -> "APPROVED".equals(t.get("status"))).count());
            result.put("pendingTopics", topicsList.stream().filter(t -> "PENDING".equals(t.get("status"))).count());
            result.put("rejectedTopics", topicsList.stream().filter(t -> "REJECTED".equals(t.get("status"))).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("topics", new java.util.ArrayList<>());
            result.put("totalTopics", 0);
            result.put("approvedTopics", 0);
            result.put("pendingTopics", 0);
            result.put("rejectedTopics", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getCreatorVocabularies(Long userId) {
        try {
            List<Vocab> vocabularies = vocabRepository.findByCreatedByAndDeletedAtIsNull(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            List<Map<String, Object>> vocabList = vocabularies.stream()
                .map(vocab -> {
                    Map<String, Object> vocabData = new HashMap<>();
                    vocabData.put("id", vocab.getId());
                    vocabData.put("vocab", vocab.getVocab());
                    vocabData.put("meaning", vocab.getMeaning());
                    vocabData.put("status", vocab.getStatus());
                    vocabData.put("createdDate", vocab.getCreatedAt().toString().substring(0, 10));
                    
                    // Get topic name
                    String topicName = vocab.getSubTopic() != null && vocab.getSubTopic().getTopic() != null 
                        ? vocab.getSubTopic().getTopic().getTopicName() 
                        : "N/A";
                    vocabData.put("topic", topicName);
                    
                    // Mock approver info for now (TODO: implement approval system)
                    vocabData.put("approver", vocab.getStatus().equals("APPROVED") ? "System" : null);
                    
                    return vocabData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("vocabularies", vocabList);
            result.put("totalVocabularies", vocabList.size());
            result.put("approvedVocabularies", vocabList.stream().filter(v -> "APPROVED".equals(v.get("status"))).count());
            result.put("pendingVocabularies", vocabList.stream().filter(v -> "PENDING".equals(v.get("status"))).count());
            result.put("rejectedVocabularies", vocabList.stream().filter(v -> "REJECTED".equals(v.get("status"))).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("vocabularies", new java.util.ArrayList<>());
            result.put("totalVocabularies", 0);
            result.put("approvedVocabularies", 0);
            result.put("pendingVocabularies", 0);
            result.put("rejectedVocabularies", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getCreatorDetailedStats(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Topic> topics = topicRepository.findByCreatedByAndDeletedAtIsNull(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
            List<Vocab> vocabularies = vocabRepository.findByCreatedByAndDeletedAtIsNull(userId, org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            long totalTopics = topics.size();
            long approvedTopics = topics.stream().filter(t -> "APPROVED".equals(t.getStatus())).count();
            long pendingTopics = topics.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
            long rejectedTopics = topics.stream().filter(t -> "REJECTED".equals(t.getStatus())).count();
            
            long totalVocabularies = vocabularies.size();
            long approvedVocabularies = vocabularies.stream().filter(v -> "APPROVED".equals(v.getStatus())).count();
            long pendingVocabularies = vocabularies.stream().filter(v -> "PENDING".equals(v.getStatus())).count();
            long rejectedVocabularies = vocabularies.stream().filter(v -> "REJECTED".equals(v.getStatus())).count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTopics", totalTopics);
            stats.put("approvedTopics", approvedTopics);
            stats.put("pendingTopics", pendingTopics);
            stats.put("rejectedTopics", rejectedTopics);
            stats.put("totalVocabularies", totalVocabularies);
            stats.put("approvedVocabularies", approvedVocabularies);
            stats.put("pendingVocabularies", pendingVocabularies);
            stats.put("rejectedVocabularies", rejectedVocabularies);
            stats.put("lastActivity", topics.isEmpty() && vocabularies.isEmpty() ? null : 
                topics.stream()
                    .map(Topic::getCreatedAt)
                    .max(Instant::compareTo)
                    .orElse(vocabularies.stream()
                        .map(Vocab::getCreatedAt)
                        .max(Instant::compareTo)
                        .orElse(null))
                    .toString().substring(0, 19).replace("T", " "));
            
            return stats;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("totalTopics", 0);
            result.put("approvedTopics", 0);
            result.put("pendingTopics", 0);
            result.put("rejectedTopics", 0);
            result.put("totalVocabularies", 0);
            result.put("approvedVocabularies", 0);
            result.put("pendingVocabularies", 0);
            result.put("rejectedVocabularies", 0);
            result.put("lastActivity", null);
            return result;
        }
    }

    @Override
    public Map<String, Object> getApproverTopics(Long userId) {
        try {
            // For now, we'll get all topics that have been approved/rejected
            // In a real system, you'd track which approver approved which topic
            List<Topic> approvedTopics = topicRepository.findByStatusAndDeletedAtIsNull("APPROVED");
            List<Topic> rejectedTopics = topicRepository.findByStatusAndDeletedAtIsNull("REJECTED");
            List<Topic> allTopics = new java.util.ArrayList<>();
            allTopics.addAll(approvedTopics);
            allTopics.addAll(rejectedTopics);
            
            List<Map<String, Object>> topicsList = allTopics.stream()
                .map(topic -> {
                    // Get creator name from user repository
                    String creatorName = "N/A";
                    if (topic.getCreatedBy() != null) {
                        try {
                            Optional<User> creator = userRepository.findById(topic.getCreatedBy());
                            if (creator.isPresent()) {
                                creatorName = creator.get().getFirstName() + " " + creator.get().getLastName();
                            }
                        } catch (Exception e) {
                            // If error, keep "N/A"
                        }
                    }
                    
                    Map<String, Object> topicData = new HashMap<>();
                    topicData.put("id", topic.getId());
                    topicData.put("name", topic.getTopicName());
                    topicData.put("status", topic.getStatus().toLowerCase());
                    topicData.put("approvalDate", topic.getUpdatedAt() != null ? 
                        topic.getUpdatedAt().toString().substring(0, 10) : "N/A");
                    
                    // Count vocabularies in this topic - simplified for now
                    topicData.put("vocabulary", 0); // TODO: Implement proper counting
                    
                    // Get creator name
                    topicData.put("creator", creatorName);
                    
                    return topicData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("topics", topicsList);
            result.put("totalTopics", topicsList.size());
            result.put("approvedTopics", topicsList.stream().filter(t -> "approved".equals(t.get("status"))).count());
            result.put("rejectedTopics", topicsList.stream().filter(t -> "rejected".equals(t.get("status"))).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("topics", new java.util.ArrayList<>());
            result.put("totalTopics", 0);
            result.put("approvedTopics", 0);
            result.put("rejectedTopics", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getApproverVocabularies(Long userId) {
        try {
            // For now, we'll get all vocabularies that have been approved/rejected
            // In a real system, you'd track which approver approved which vocabulary
            List<Vocab> approvedVocabularies = vocabRepository.findByStatusAndDeletedAtIsNull("APPROVED", org.springframework.data.domain.Pageable.unpaged()).getContent();
            List<Vocab> rejectedVocabularies = vocabRepository.findByStatusAndDeletedAtIsNull("REJECTED", org.springframework.data.domain.Pageable.unpaged()).getContent();
            List<Vocab> allVocabularies = new java.util.ArrayList<>();
            allVocabularies.addAll(approvedVocabularies);
            allVocabularies.addAll(rejectedVocabularies);
            
            List<Map<String, Object>> vocabList = allVocabularies.stream()
                .map(vocab -> {
                    // Get creator name from user repository
                    String creatorName = "N/A";
                    if (vocab.getCreatedBy() != null) {
                        try {
                            Optional<User> creator = userRepository.findById(vocab.getCreatedBy());
                            if (creator.isPresent()) {
                                creatorName = creator.get().getFirstName() + " " + creator.get().getLastName();
                            }
                        } catch (Exception e) {
                            // If error, keep "N/A"
                        }
                    }
                    
                    Map<String, Object> vocabData = new HashMap<>();
                    vocabData.put("id", vocab.getId());
                    vocabData.put("vocab", vocab.getVocab());
                    vocabData.put("meaning", vocab.getMeaning());
                    vocabData.put("status", vocab.getStatus().toLowerCase());
                    vocabData.put("approvalDate", vocab.getUpdatedAt() != null ? 
                        vocab.getUpdatedAt().toString().substring(0, 10) : "N/A");
                    
                    // Get topic name
                    String topicName = vocab.getSubTopic() != null && vocab.getSubTopic().getTopic() != null 
                        ? vocab.getSubTopic().getTopic().getTopicName() 
                        : "N/A";
                    vocabData.put("topic", topicName);
                    
                    // Get creator name
                    vocabData.put("creator", creatorName);
                    
                    return vocabData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("vocabularies", vocabList);
            result.put("totalVocabularies", vocabList.size());
            result.put("approvedVocabularies", vocabList.stream().filter(v -> "approved".equals(v.get("status"))).count());
            result.put("rejectedVocabularies", vocabList.stream().filter(v -> "rejected".equals(v.get("status"))).count());
            
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("vocabularies", new java.util.ArrayList<>());
            result.put("totalVocabularies", 0);
            result.put("approvedVocabularies", 0);
            result.put("rejectedVocabularies", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getApproverDetailedStats(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get approved/rejected topics and vocabularies
            List<Topic> approvedTopics = topicRepository.findByStatusAndDeletedAtIsNull("APPROVED");
            List<Topic> rejectedTopics = topicRepository.findByStatusAndDeletedAtIsNull("REJECTED");
            List<Topic> allTopics = new java.util.ArrayList<>();
            allTopics.addAll(approvedTopics);
            allTopics.addAll(rejectedTopics);
            
            List<Vocab> approvedVocabularies = vocabRepository.findByStatusAndDeletedAtIsNull("APPROVED", org.springframework.data.domain.Pageable.unpaged()).getContent();
            List<Vocab> rejectedVocabularies = vocabRepository.findByStatusAndDeletedAtIsNull("REJECTED", org.springframework.data.domain.Pageable.unpaged()).getContent();
            List<Vocab> allVocabularies = new java.util.ArrayList<>();
            allVocabularies.addAll(approvedVocabularies);
            allVocabularies.addAll(rejectedVocabularies);
            
            long totalTopics = allTopics.size();
            long approvedTopicsCount = approvedTopics.size();
            long rejectedTopicsCount = rejectedTopics.size();
            
            long totalVocabularies = allVocabularies.size();
            long approvedVocabulariesCount = approvedVocabularies.size();
            long rejectedVocabulariesCount = rejectedVocabularies.size();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTopics", totalTopics);
            stats.put("approvedTopics", approvedTopicsCount);
            stats.put("rejectedTopics", rejectedTopicsCount);
            stats.put("totalVocabularies", totalVocabularies);
            stats.put("approvedVocabularies", approvedVocabulariesCount);
            stats.put("rejectedVocabularies", rejectedVocabulariesCount);
            stats.put("lastActivity", allTopics.isEmpty() && allVocabularies.isEmpty() ? null : 
                allTopics.stream()
                    .map(Topic::getCreatedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(allVocabularies.stream()
                        .map(Vocab::getCreatedAt)
                        .filter(java.util.Objects::nonNull)
                        .max(Instant::compareTo)
                        .orElse(null))
                    .toString().substring(0, 19).replace("T", " "));
            
            return stats;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", e.getMessage());
            result.put("totalTopics", 0);
            result.put("approvedTopics", 0);
            result.put("rejectedTopics", 0);
            result.put("totalVocabularies", 0);
            result.put("approvedVocabularies", 0);
            result.put("rejectedVocabularies", 0);
            result.put("lastActivity", null);
            return result;
        }
    }
} 