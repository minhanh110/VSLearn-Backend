package com.vslearn.controller;

import com.vslearn.dto.request.NotificationCreateRequest;
import com.vslearn.dto.response.NotificationResponse;
import com.vslearn.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    // Tạo thông báo mới
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody NotificationCreateRequest request) {
        try {
            NotificationResponse response = notificationService.createNotification(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo thông báo thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Lấy thông báo theo ID
    @GetMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable Long notificationId) {
        try {
            NotificationResponse response = notificationService.getNotificationById(notificationId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Lấy danh sách thông báo của user (có phân trang)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationResponse> response = notificationService.getNotificationsByToUserId(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Lấy thông báo chưa gửi của user
    @GetMapping("/user/{userId}/unsent")
    public ResponseEntity<Map<String, Object>> getUnsentNotifications(@PathVariable Long userId) {
        try {
            List<NotificationResponse> response = notificationService.getUnsentNotificationsByToUserId(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Lấy thông báo đã gửi của user (có phân trang)
    @GetMapping("/user/{userId}/sent")
    public ResponseEntity<Map<String, Object>> getSentNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationResponse> response = notificationService.getSentNotificationsByToUserId(userId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Đếm số thông báo chưa gửi
    @GetMapping("/user/{userId}/count-unsent")
    public ResponseEntity<Map<String, Object>> countUnsentNotifications(@PathVariable Long userId) {
        try {
            Long count = notificationService.countUnsentNotificationsByToUserId(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("count", count)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Đánh dấu thông báo đã gửi
    @PutMapping("/{notificationId}/mark-sent")
    public ResponseEntity<Map<String, Object>> markAsSent(@PathVariable Long notificationId) {
        try {
            notificationService.markAsSent(notificationId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đánh dấu thông báo đã gửi thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Đánh dấu tất cả thông báo của user đã gửi
    @PutMapping("/user/{userId}/mark-all-sent")
    public ResponseEntity<Map<String, Object>> markAllAsSent(@PathVariable Long userId) {
        try {
            notificationService.markAllAsSent(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đánh dấu tất cả thông báo đã gửi thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Xóa thông báo
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa thông báo thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
} 