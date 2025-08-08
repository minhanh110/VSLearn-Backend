package com.vslearn.service.impl;

import com.vslearn.dto.AIResponseDTO;
import com.vslearn.dto.VideoProcessingDTO;
import com.vslearn.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class AIServiceImpl implements AIService {
    
    private final String AI_SERVICE_URL = "http://localhost:8001";
    private final RestTemplate restTemplate;
    
    @Autowired
    public AIServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public VideoProcessingDTO processVideo(MultipartFile videoFile, String expectedWord, String category, String difficulty) {
        try {
            // Tạo request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Tạo multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(videoFile.getBytes()) {
                @Override
                public String getFilename() {
                    return videoFile.getOriginalFilename();
                }
            };
            body.add("video", fileResource);
            
            // Thêm metadata
            body.add("expectedWord", expectedWord);
            body.add("category", category);
            body.add("difficulty", difficulty);
            
            // Tạo HTTP entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Gọi AI service
            ResponseEntity<Map> response = restTemplate.postForEntity(
                AI_SERVICE_URL + "/process-video",
                requestEntity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            return new VideoProcessingDTO(
                (String) responseBody.get("task_id"),
                (String) responseBody.get("status"),
                (String) responseBody.get("message"),
                expectedWord,
                category,
                difficulty
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit video to AI service: " + e.getMessage());
        }
    }
    
    @Override
    public AIResponseDTO checkProcessingStatus(String taskId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                AI_SERVICE_URL + "/status/" + taskId,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            AIResponseDTO aiResponse = new AIResponseDTO();
            aiResponse.setTaskId(taskId);
            aiResponse.setStatus((String) responseBody.get("status"));
            aiResponse.setProgress((Integer) responseBody.get("progress"));
            aiResponse.setError((String) responseBody.get("error"));
            aiResponse.setCreatedAt((String) responseBody.get("created_at"));
            
            // Parse result nếu có
            if (responseBody.get("result") != null) {
                Map<String, Object> resultMap = (Map<String, Object>) responseBody.get("result");
                AIResponseDTO.RecognitionResult result = new AIResponseDTO.RecognitionResult();
                result.setPredictedWord((String) resultMap.get("predicted_word"));
                result.setConfidence((Double) resultMap.get("confidence"));
                result.setOverallAccuracy((Double) resultMap.get("overall_accuracy"));
                result.setFeedback((String) resultMap.get("feedback"));
                aiResponse.setResult(result);
            }
            
            return aiResponse;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to check status: " + e.getMessage());
        }
    }
    
    @Override
    public boolean checkAIServiceHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                AI_SERVICE_URL + "/health",
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            return "healthy".equals(responseBody.get("status"));
            
        } catch (Exception e) {
            return false;
        }
    }
} 