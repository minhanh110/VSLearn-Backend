package com.vslearn.controller;

import com.vslearn.dto.response.AdminDashboardResponse;
import com.vslearn.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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
} 