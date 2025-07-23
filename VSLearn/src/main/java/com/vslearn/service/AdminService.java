package com.vslearn.service;

import com.vslearn.dto.response.AdminDashboardResponse;
import com.vslearn.dto.request.UserManagementRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();
    Map<String, Object> getUserStats();
    Map<String, Object> getContentStats();
    Map<String, Object> getLearningStats();
    
    // ==================== USER MANAGEMENT ====================
    
    // Learners Management
    Map<String, Object> getLearnersList(int page, int size, String search, Boolean isActive);
    Map<String, Object> getLearnersStats();
    
    // Creators Management
    Map<String, Object> getCreatorsList(int page, int size, String search, Boolean isActive);
    Map<String, Object> getCreatorsStats();
    
    // Approvers Management
    Map<String, Object> getApproversList(int page, int size, String search, Boolean isActive);
    Map<String, Object> getApproversStats();
    
    // All Users Management
    Map<String, Object> getAllUsersList(int page, int size, String search, String userRole, Boolean isActive);
    
    // User Detail Management
    Map<String, Object> getUserById(Long userId);

    ResponseEntity<?> createUserByManager(UserManagementRequest req);
    ResponseEntity<?> updateUserByManager(Long userId, UserManagementRequest req);
} 