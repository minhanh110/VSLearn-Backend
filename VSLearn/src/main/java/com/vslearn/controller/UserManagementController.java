package com.vslearn.controller;

import com.vslearn.dto.request.UserManagementRequest;
import com.vslearn.dto.request.UserUpdateRequest;
import com.vslearn.dto.response.UserManagementResponse;
import com.vslearn.dto.response.UserListResponse;
import com.vslearn.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-management")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    @Autowired
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }
    
    // ==================== LEARNERS MANAGEMENT ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/learners")
    public ResponseEntity<UserListResponse> getLearnersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        UserListResponse response = userManagementService.getLearnersList(pageable, search, isActive);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/learners/stats")
    public ResponseEntity<Map<String, Object>> getLearnersStats() {
        Map<String, Object> stats = userManagementService.getLearnersStats();
        return ResponseEntity.ok(stats);
    }
    
    // ==================== CREATORS MANAGEMENT ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/creators")
    public ResponseEntity<UserListResponse> getCreatorsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        UserListResponse response = userManagementService.getCreatorsList(pageable, search, isActive);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/creators/stats")
    public ResponseEntity<Map<String, Object>> getCreatorsStats() {
        Map<String, Object> stats = userManagementService.getCreatorsStats();
        return ResponseEntity.ok(stats);
    }
    
    // ==================== APPROVERS MANAGEMENT ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/approvers")
    public ResponseEntity<UserListResponse> getApproversList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        UserListResponse response = userManagementService.getApproversList(pageable, search, isActive);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/approvers/stats")
    public ResponseEntity<Map<String, Object>> getApproversStats() {
        Map<String, Object> stats = userManagementService.getApproversStats();
        return ResponseEntity.ok(stats);
    }
    
    // ==================== GENERAL USER MANAGEMENT ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users")
    public ResponseEntity<UserListResponse> getAllUsersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        UserListResponse response = userManagementService.getAllUsersList(pageable, search, userRole, isActive);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = userManagementService.getUserStats();
        return ResponseEntity.ok(stats);
    }
    
    // ==================== CRUD OPERATIONS ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid UserManagementRequest request) {
        try {
            UserManagementResponse response = userManagementService.createUser(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo người dùng thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserManagementResponse> getUserById(@PathVariable Long userId) {
        UserManagementResponse response = userManagementService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateRequest request) {
        try {
            UserManagementResponse response = userManagementService.updateUser(userId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật người dùng thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            userManagementService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa người dùng thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // ==================== USER STATUS MANAGEMENT ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long userId) {
        try {
            UserManagementResponse response = userManagementService.toggleUserStatus(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật trạng thái người dùng thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String newRole) {
        try {
            UserManagementResponse response = userManagementService.updateUserRole(userId, newRole);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật vai trò người dùng thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // ==================== EXPORT FUNCTIONALITY ====================
    
    @PreAuthorize("hasAnyAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsersToExcel(
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Boolean isActive) {
        try {
            byte[] excelData = userManagementService.exportUsersToExcel(userRole, isActive);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "users_export.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 