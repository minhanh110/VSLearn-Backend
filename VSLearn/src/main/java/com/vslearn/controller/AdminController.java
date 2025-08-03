package com.vslearn.controller;

import com.vslearn.dto.response.AdminDashboardResponse;
import com.vslearn.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.vslearn.dto.request.UserManagementRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import com.vslearn.dto.response.ResponseData;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AdminController {
    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Lấy thống kê dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        AdminDashboardResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    // Lấy thống kê người dùng
    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = adminService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    // Lấy thống kê nội dung
    @GetMapping("/content/stats")
    public ResponseEntity<Map<String, Object>> getContentStats() {
        Map<String, Object> stats = adminService.getContentStats();
        return ResponseEntity.ok(stats);
    }

    // Lấy thống kê học tập
    @GetMapping("/learning/stats")
    public ResponseEntity<Map<String, Object>> getLearningStats() {
        Map<String, Object> stats = adminService.getLearningStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== USER MANAGEMENT ====================
    
    // Learners Management
    @GetMapping("/users/learners")
    public ResponseEntity<Map<String, Object>> getLearnersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        Map<String, Object> result = adminService.getLearnersList(page, size, search, isActive);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/users/learners/stats")
    public ResponseEntity<Map<String, Object>> getLearnersStats() {
        Map<String, Object> stats = adminService.getLearnersStats();
        return ResponseEntity.ok(stats);
    }
    
    // Creators Management
    @GetMapping("/users/creators")
    public ResponseEntity<Map<String, Object>> getCreatorsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        Map<String, Object> result = adminService.getCreatorsList(page, size, search, isActive);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/users/creators/stats")
    public ResponseEntity<Map<String, Object>> getCreatorsStats() {
        Map<String, Object> stats = adminService.getCreatorsStats();
        return ResponseEntity.ok(stats);
    }
    
    // Approvers Management
    @GetMapping("/users/approvers")
    public ResponseEntity<Map<String, Object>> getApproversList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        Map<String, Object> result = adminService.getApproversList(page, size, search, isActive);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/users/approvers/stats")
    public ResponseEntity<Map<String, Object>> getApproversStats() {
        Map<String, Object> stats = adminService.getApproversStats();
        return ResponseEntity.ok(stats);
    }
    
    // All Users Management
    @GetMapping("/users/all")
    public ResponseEntity<Map<String, Object>> getAllUsersList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Boolean isActive) {
        Map<String, Object> result = adminService.getAllUsersList(page, size, search, userRole, isActive);
        return ResponseEntity.ok(result);
    }
    
    // User Detail Management
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getUserById(userId);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @PostMapping("/users/create")
    public ResponseEntity<?> createUserByManager(@RequestBody @Valid UserManagementRequest req) {
        return adminService.createUserByManager(req);
    }

    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUserByManager(@PathVariable Long userId, @RequestBody @Valid UserManagementRequest req) {
        return adminService.updateUserByManager(userId, req);
    }

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Admin API is working"
        ));
    }

    @GetMapping("/users/learners/test")
    public ResponseEntity<Map<String, Object>> testLearnersEndpoint() {
        Map<String, Object> data = Map.of(
            "content", new Object[0],
            "totalElements", 0,
            "totalPages", 0,
            "currentPage", 0,
            "pageSize", 10
        );
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Learners API is working",
            "data", data
        ));
    }

    @GetMapping("/users/creators/test")
    public ResponseEntity<Map<String, Object>> testCreatorsEndpoint() {
        Map<String, Object> data = Map.of(
            "content", new Object[0],
            "totalElements", 0,
            "totalPages", 0,
            "currentPage", 0,
            "pageSize", 10
        );
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Creators API is working",
            "data", data
        ));
    }

    // ==================== LEARNER DETAILS ====================
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/packages")
    public ResponseEntity<Map<String, Object>> getLearnerPackages(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getLearnerPackages(userId);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/activities")
    public ResponseEntity<Map<String, Object>> getLearnerActivities(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getLearnerActivities(userId);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/detailed-stats")
    public ResponseEntity<Map<String, Object>> getLearnerDetailedStats(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getLearnerDetailedStats(userId);
        return ResponseEntity.ok(result);
    }

    // ==================== CREATOR DETAILS ====================
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/topics")
    public ResponseEntity<Map<String, Object>> getCreatorTopics(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getCreatorTopics(userId);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/vocabularies")
    public ResponseEntity<Map<String, Object>> getCreatorVocabularies(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getCreatorVocabularies(userId);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/creator-stats")
    public ResponseEntity<Map<String, Object>> getCreatorDetailedStats(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getCreatorDetailedStats(userId);
        return ResponseEntity.ok(result);
    }

    // ==================== APPROVER DETAILS ====================
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/approved-topics")
    public ResponseEntity<Map<String, Object>> getApproverTopics(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getApproverTopics(userId);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/approved-vocabularies")
    public ResponseEntity<Map<String, Object>> getApproverVocabularies(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getApproverVocabularies(userId);
        return ResponseEntity.ok(result);
    }
    
    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/users/{userId}/approver-stats")
    public ResponseEntity<Map<String, Object>> getApproverDetailedStats(@PathVariable Long userId) {
        Map<String, Object> result = adminService.getApproverDetailedStats(userId);
        return ResponseEntity.ok(result);
    }
} 