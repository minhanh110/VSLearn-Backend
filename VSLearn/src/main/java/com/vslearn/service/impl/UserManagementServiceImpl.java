package com.vslearn.service.impl;

import com.vslearn.dto.request.UserManagementRequest;
import com.vslearn.dto.request.UserUpdateRequest;
import com.vslearn.dto.response.UserManagementResponse;
import com.vslearn.dto.response.UserListResponse;
import com.vslearn.entities.User;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final TopicRepository topicRepository;
    private final VocabRepository vocabRepository;
    
    @Autowired
    public UserManagementServiceImpl(UserRepository userRepository, ProgressRepository progressRepository,
                                  TopicRepository topicRepository, VocabRepository vocabRepository) {
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.topicRepository = topicRepository;
        this.vocabRepository = vocabRepository;
    }
    
    @Override
    public UserManagementResponse createUser(UserManagementRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByUserEmail(request.getUserEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .userName(request.getUserName())
                .userEmail(request.getUserEmail())
                .phoneNumber(request.getPhoneNumber())
                .userRole(request.getUserRole())
                .userAvatar(request.getUserAvatar())
                .isActive(request.getIsActive())
                .provider("LOCAL")
                .createdAt(Instant.now())
                .createdBy(1L) // TODO: Get from current user context
                .build();
        
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }
    
    @Override
    public UserManagementResponse updateUser(Long userId, UserUpdateRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        User user = optionalUser.get();
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getUserName() != null) {
            if (!request.getUserName().equals(user.getUserName()) && 
                userRepository.existsByUserName(request.getUserName())) {
                throw new RuntimeException("Username already exists");
            }
            user.setUserName(request.getUserName());
        }
        if (request.getUserEmail() != null) {
            if (!request.getUserEmail().equals(user.getUserEmail()) && 
                userRepository.existsByUserEmail(request.getUserEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setUserEmail(request.getUserEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getUserRole() != null) {
            user.setUserRole(request.getUserRole());
        }
        if (request.getUserAvatar() != null) {
            user.setUserAvatar(request.getUserAvatar());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(1L); // TODO: Get from current user context
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }
    
    @Override
    public void deleteUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        User user = optionalUser.get();
        user.setDeletedAt(Instant.now());
        user.setDeletedBy(1L); // TODO: Get from current user context
        user.setIsActive(false);
        
        userRepository.save(user);
    }
    
    @Override
    public UserManagementResponse getUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        return convertToResponse(optionalUser.get());
    }
    
    @Override
    public UserListResponse getLearnersList(Pageable pageable, String search, Boolean isActive) {
        return getUserListByRole("ROLE_LEARNER", pageable, search, isActive);
    }
    
    @Override
    public UserListResponse getCreatorsList(Pageable pageable, String search, Boolean isActive) {
        return getUserListByRole("ROLE_CONTENT_CREATOR", pageable, search, isActive);
    }
    
    @Override
    public UserListResponse getApproversList(Pageable pageable, String search, Boolean isActive) {
        return getUserListByRole("ROLE_CONTENT_APPROVER", pageable, search, isActive);
    }
    
    @Override
    public UserListResponse getManagersList(Pageable pageable, String search, Boolean isActive) {
        return getUserListByRole("ROLE_GENERAL_MANAGER", pageable, search, isActive);
    }
    
    @Override
    public UserListResponse getAllUsersList(Pageable pageable, String search, String userRole, Boolean isActive) {
        // TODO: Implement with proper filtering
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserManagementResponse> users = userPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return UserListResponse.builder()
                .users(users)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByUserRole("ROLE_LEARNER")) // TODO: Fix this
                .inactiveUsers(0L) // TODO: Implement
                .learnersCount(userRepository.countByUserRole("ROLE_LEARNER"))
                .creatorsCount(userRepository.countByUserRole("ROLE_CONTENT_CREATOR"))
                .approversCount(userRepository.countByUserRole("ROLE_CONTENT_APPROVER"))
                .managersCount(userRepository.countByUserRole("ROLE_GENERAL_MANAGER"))
                .build();
    }
    
    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByUserRole("ROLE_LEARNER")); // TODO: Fix
        stats.put("inactiveUsers", 0L); // TODO: Implement
        stats.put("learnersCount", userRepository.countByUserRole("ROLE_LEARNER"));
        stats.put("creatorsCount", userRepository.countByUserRole("ROLE_CONTENT_CREATOR"));
        stats.put("approversCount", userRepository.countByUserRole("ROLE_CONTENT_APPROVER"));
        stats.put("managersCount", userRepository.countByUserRole("ROLE_GENERAL_MANAGER"));
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getLearnersStats() {
        Map<String, Object> stats = new HashMap<>();
        long learnersCount = userRepository.countByUserRole("ROLE_LEARNER");
        stats.put("totalLearners", learnersCount);
        stats.put("activeLearners", learnersCount); // TODO: Implement active count
        stats.put("inactiveLearners", 0L); // TODO: Implement
        stats.put("averageTopicsCompleted", 0L); // TODO: Calculate from progress
        stats.put("averagePackagesOwned", 0L); // TODO: Calculate from transactions
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getCreatorsStats() {
        Map<String, Object> stats = new HashMap<>();
        long creatorsCount = userRepository.countByUserRole("ROLE_CONTENT_CREATOR");
        stats.put("totalCreators", creatorsCount);
        stats.put("activeCreators", creatorsCount); // TODO: Implement active count
        stats.put("inactiveCreators", 0L); // TODO: Implement
        stats.put("totalTopicsCreated", topicRepository.count()); // TODO: Filter by creator
        stats.put("totalVocabularyCreated", vocabRepository.count()); // TODO: Filter by creator
        stats.put("pendingApproval", 0L); // TODO: Calculate from topics
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getApproversStats() {
        Map<String, Object> stats = new HashMap<>();
        long approversCount = userRepository.countByUserRole("ROLE_CONTENT_APPROVER");
        stats.put("totalApprovers", approversCount);
        stats.put("activeApprovers", approversCount); // TODO: Implement active count
        stats.put("inactiveApprovers", 0L); // TODO: Implement
        stats.put("totalTopicsApproved", 0L); // TODO: Calculate from topics
        stats.put("pendingReview", 0L); // TODO: Calculate from topics
        
        return stats;
    }
    
    @Override
    public UserManagementResponse toggleUserStatus(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        User user = optionalUser.get();
        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(1L); // TODO: Get from current user context
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }
    
    @Override
    public UserManagementResponse updateUserRole(Long userId, String newRole) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        User user = optionalUser.get();
        user.setUserRole(newRole);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(1L); // TODO: Get from current user context
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }
    
    @Override
    public byte[] exportUsersToExcel(String userRole, Boolean isActive) {
        // TODO: Implement Excel export
        return new byte[0];
    }
    
    private UserListResponse getUserListByRole(String role, Pageable pageable, String search, Boolean isActive) {
        // TODO: Implement proper filtering with search and isActive
        List<User> users = userRepository.findByIsActive(isActive != null ? isActive : true);
        
        List<UserManagementResponse> userResponses = users.stream()
                .filter(user -> user.getUserRole().equals(role))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return UserListResponse.builder()
                .users(userResponses)
                .totalElements((long) userResponses.size())
                .totalPages(1)
                .currentPage(0)
                .pageSize(userResponses.size())
                .totalUsers(userRepository.countByUserRole(role))
                .activeUsers(userRepository.countByUserRole(role)) // TODO: Fix
                .inactiveUsers(0L) // TODO: Implement
                .learnersCount(userRepository.countByUserRole("ROLE_LEARNER"))
                .creatorsCount(userRepository.countByUserRole("ROLE_CONTENT_CREATOR"))
                .approversCount(userRepository.countByUserRole("ROLE_CONTENT_APPROVER"))
                .managersCount(userRepository.countByUserRole("ROLE_GENERAL_MANAGER"))
                .build();
    }
    
    private UserManagementResponse convertToResponse(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .phoneNumber(user.getPhoneNumber())
                .userRole(user.getUserRole())
                .userAvatar(user.getUserAvatar())
                .isActive(user.getIsActive())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .topicsCompleted(0L) // TODO: Calculate from progress
                .packagesOwned(0L) // TODO: Calculate from transactions
                .topicsCreated(0L) // TODO: Calculate from topics
                .vocabularyCreated(0L) // TODO: Calculate from vocab
                .pendingApproval(0L) // TODO: Calculate from topics
                .topicsApproved(0L) // TODO: Calculate from topics
                .pendingReview(0L) // TODO: Calculate from topics
                .specialization("N/A") // TODO: Add specialization field to User entity
                .build();
    }
} 