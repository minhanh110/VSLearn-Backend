package com.vslearn.controller;

import com.vslearn.dto.request.NotifyCreateRequest;
import com.vslearn.dto.response.NotifyResponse;
import com.vslearn.service.NotifyService;
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
@RequestMapping("/api/v1/notify")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotifyController {
    
    private final NotifyService notifyService;
    
    @Autowired
    public NotifyController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }
    
    // Tạo thông báo mới
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    public ResponseEntity<Map<String, Object>> createNotify(@RequestBody NotifyCreateRequest request) {
        try {
            NotifyResponse response = notifyService.createNotify(request);
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
    @GetMapping("/{notifyId}")
    public ResponseEntity<Map<String, Object>> getNotifyById(@PathVariable Long notifyId) {
        try {
            NotifyResponse response = notifyService.getNotifyById(notifyId);
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
    public ResponseEntity<Map<String, Object>> getNotifiesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotifyResponse> response = notifyService.getNotifiesByToUserId(userId, pageable);
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
    public ResponseEntity<Map<String, Object>> getUnsentNotifies(@PathVariable Long userId) {
        try {
            List<NotifyResponse> response = notifyService.getUnsentNotifiesByToUserId(userId);
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
    public ResponseEntity<Map<String, Object>> getSentNotifies(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotifyResponse> response = notifyService.getSentNotifiesByToUserId(userId, pageable);
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
    public ResponseEntity<Map<String, Object>> countUnsentNotifies(@PathVariable Long userId) {
        try {
            Long count = notifyService.countUnsentNotifiesByToUserId(userId);
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
    @PutMapping("/{notifyId}/mark-sent")
    public ResponseEntity<Map<String, Object>> markAsSent(@PathVariable Long notifyId) {
        try {
            notifyService.markAsSent(notifyId);
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
            notifyService.markAllAsSent(userId);
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
    @DeleteMapping("/{notifyId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteNotify(@PathVariable Long notifyId) {
        try {
            notifyService.deleteNotify(notifyId);
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