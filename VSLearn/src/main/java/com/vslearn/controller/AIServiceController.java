package com.vslearn.controller;

import com.vslearn.dto.AIResponseDTO;
import com.vslearn.dto.VideoProcessingDTO;
import com.vslearn.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
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
            // Validate file
            if (videoFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Video file is required"));
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
} 