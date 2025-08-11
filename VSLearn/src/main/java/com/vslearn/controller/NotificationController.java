package com.vslearn.controller;

import com.vslearn.dto.request.NotificationCreateRequest;
import com.vslearn.dto.response.NotificationResponse;
import com.vslearn.service.NotificationService;
import com.vslearn.utils.JwtUtil;
import com.vslearn.entities.User;
import com.vslearn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Autowired
    public NotificationController(NotificationService notificationService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // Lấy thông báo của user hiện tại
    @GetMapping("/my-notifications")
    public ResponseEntity<Map<String, Object>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Get user ID from email (Spring Security stores email in authentication name)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Notification API - Email from authentication: " + email);
            
            // Get user ID from database by email
            User user = userRepository.findByUserEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy user với email: " + email
                ));
            }
            
            Long userId = user.getId();
            System.out.println("Notification API - User ID from database: " + userId);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationResponse> response = notificationService.getNotificationsByToUserId(userId, pageable);
            System.out.println("Notification API - Found " + response.getContent().size() + " notifications for user " + userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response.getContent()
            ));
        } catch (Exception e) {
            System.err.println("Notification API - Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Đếm thông báo chưa đọc của user hiện tại
    @GetMapping("/my-notifications/count-unsent")
    public ResponseEntity<Map<String, Object>> countMyUnsentNotifications(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Get user ID from email (Spring Security stores email in authentication name)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Count API - Email from authentication: " + email);
            
            // Get user ID from database by email
            User user = userRepository.findByUserEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy user với email: " + email
                ));
            }
            
            Long userId = user.getId();
            System.out.println("Count API - User ID from database: " + userId);
            
            Long count = notificationService.countUnsentNotificationsByToUserId(userId);
            System.out.println("Count API - Found " + count + " unsent notifications for user " + userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("count", count)
            ));
        } catch (Exception e) {
            System.err.println("Count API - Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Đánh dấu tất cả thông báo của user hiện tại đã đọc
    @PutMapping("/my-notifications/mark-all-sent")
    public ResponseEntity<Map<String, Object>> markAllMyNotificationsAsSent(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Get user ID from email (Spring Security stores email in authentication name)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Mark all sent API - Email from authentication: " + email);
            
            // Get user ID from database by email
            User user = userRepository.findByUserEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy user với email: " + email
                ));
            }
            
            Long userId = user.getId();
            System.out.println("Mark all sent API - User ID from database: " + userId);
            
            notificationService.markAllAsSent(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đánh dấu tất cả thông báo đã đọc thành công"
            ));
        } catch (Exception e) {
            System.err.println("Mark all sent API - Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
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