package com.vslearn.service;

import com.vslearn.dto.request.UserManagementRequest;
import com.vslearn.dto.request.UserUpdateRequest;
import com.vslearn.dto.response.UserManagementResponse;
import com.vslearn.dto.response.UserListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserManagementService {
    
    // CRUD Operations
    UserManagementResponse createUser(UserManagementRequest request);
    UserManagementResponse updateUser(Long userId, UserUpdateRequest request);
    void deleteUser(Long userId);
    UserManagementResponse getUserById(Long userId);
    
    // List Operations by Role
    UserListResponse getLearnersList(Pageable pageable, String search, Boolean isActive);
    UserListResponse getCreatorsList(Pageable pageable, String search, Boolean isActive);
    UserListResponse getApproversList(Pageable pageable, String search, Boolean isActive);
    UserListResponse getManagersList(Pageable pageable, String search, Boolean isActive);
    
    // General User List
    UserListResponse getAllUsersList(Pageable pageable, String search, String userRole, Boolean isActive);
    
    // Statistics
    Map<String, Object> getUserStats();
    Map<String, Object> getLearnersStats();
    Map<String, Object> getCreatorsStats();
    Map<String, Object> getApproversStats();
    
    // User Status Management
    UserManagementResponse toggleUserStatus(Long userId);
    UserManagementResponse updateUserRole(Long userId, String newRole);
    
    // Export
    byte[] exportUsersToExcel(String userRole, Boolean isActive);
} 