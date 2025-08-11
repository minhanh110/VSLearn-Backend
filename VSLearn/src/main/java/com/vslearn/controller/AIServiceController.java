package com.vslearn.controller;

import com.vslearn.dto.AIResponseDTO;
import com.vslearn.dto.VideoProcessingDTO;
import com.vslearn.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@PreAuthorize("permitAll()")
public class AIServiceController {
    
    @Autowired
    private AIService aiService;
    
    /**
     * Upload video để xử lý AI
     */
    @PostMapping("/process-video")
    public ResponseEntity<?> processVideo(
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam("expectedWord") String expectedWord,
            @RequestParam("category") String category,
            @RequestParam("difficulty") String difficulty) {
        try {
            // File size validation (50MB)
            if (videoFile.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "File quá lớn. Tối đa 50MB"
                ));
            }
            
            // Validate file type
            String contentType = videoFile.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Please upload a video file"));
            }
            
            // Process video
            VideoProcessingDTO result = aiService.processVideo(videoFile, expectedWord, category, difficulty);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Kiểm tra trạng thái xử lý
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<?> checkStatus(@PathVariable String taskId) {
        try {
            AIResponseDTO result = aiService.checkProcessingStatus(taskId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Health check cho AI service
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            boolean isHealthy = aiService.checkAIServiceHealth();
            Map<String, Object> response = new HashMap<>();
            response.put("status", isHealthy ? "healthy" : "unhealthy");
            response.put("ai_service_available", isHealthy);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "unhealthy");
            response.put("ai_service_available", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Test endpoint để kiểm tra kết nối
     */
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "AI Service Controller is working");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint để kiểm tra kết nối trực tiếp đến AI service
     */
    @GetMapping("/test-ai-connection")
    public ResponseEntity<?> testAIConnection() {
        try {
            boolean isHealthy = aiService.checkAIServiceHealth();
            Map<String, Object> response = new HashMap<>();
            response.put("ai_service_available", isHealthy);
            response.put("message", isHealthy ? "AI service is healthy" : "AI service is not available");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("ai_service_available", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Simple test endpoint không gọi AI service
     */
    @GetMapping("/simple-test")
    public ResponseEntity<?> simpleTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "AI Controller is working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("ai_service_url", "http://localhost:8001");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exception handler for 413 Payload Too Large
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "File quá lớn. Tối đa 50MB");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
} 