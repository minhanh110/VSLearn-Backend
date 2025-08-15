package com.vslearn.controller;

import com.vslearn.dto.request.SentenceCreateRequest;
import com.vslearn.dto.request.SentenceUpdateRequest;
import com.vslearn.dto.response.SentenceDetailResponse;
import com.vslearn.dto.response.SentenceListResponse;
import com.vslearn.dto.response.VideoUploadResponse;
import com.vslearn.service.SentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sentences")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SentenceController {
    
    private final SentenceService sentenceService;
    
    @Autowired
    public SentenceController(SentenceService sentenceService) {
        this.sentenceService = sentenceService;
    }
    
    // Lấy danh sách sentences với phân trang và filter
    @GetMapping("/list")
    public ResponseEntity<SentenceListResponse> getSentenceList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long createdBy
    ) {
        Pageable pageable = PageRequest.of(page, size);
        SentenceListResponse response = sentenceService.getSentenceList(pageable, search, topic, status, createdBy);
        return ResponseEntity.ok(response);
    }
    
    // Lấy chi tiết sentence theo ID
    @GetMapping("/{sentenceId}")
    public ResponseEntity<SentenceDetailResponse> getSentenceDetail(@PathVariable Long sentenceId) {
        try {
            SentenceDetailResponse response = sentenceService.getSentenceDetail(sentenceId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Tạo sentence mới
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSentence(@RequestBody SentenceCreateRequest request) {
        try {
            SentenceDetailResponse response = sentenceService.createSentence(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo câu thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Cập nhật sentence
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PutMapping("/{sentenceId}")
    public ResponseEntity<Map<String, Object>> updateSentence(
            @PathVariable Long sentenceId,
            @RequestBody SentenceUpdateRequest request) {
        try {
            SentenceDetailResponse response = sentenceService.updateSentence(sentenceId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật câu thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Xóa sentence
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @DeleteMapping("/{sentenceId}")
    public ResponseEntity<Map<String, Object>> deleteSentence(@PathVariable Long sentenceId) {
        Map<String, Object> response = sentenceService.deleteSentence(sentenceId);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Upload video cho sentence
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/upload-video")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // File size validation (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "File quá lớn. Tối đa 10MB"
                ));
            }
            
            // Content type validation
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ chấp nhận file video"
                ));
            }
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            VideoUploadResponse response = sentenceService.uploadVideoToGCS(file, fileName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Upload video thành công",
                "data", response
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Lấy sentences theo topic ID
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<SentenceDetailResponse>> getSentencesByTopicId(@PathVariable Long topicId) {
        List<SentenceDetailResponse> sentences = sentenceService.getSentencesByTopicId(topicId);
        return ResponseEntity.ok(sentences);
    }
    
    // Kiểm tra topic có sentences không
    @GetMapping("/topic/{topicId}/exists")
    public ResponseEntity<Map<String, Boolean>> existsByTopicId(@PathVariable Long topicId) {
        boolean exists = sentenceService.existsByTopicId(topicId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
} 