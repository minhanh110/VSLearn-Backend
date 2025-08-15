package com.vslearn.service.impl;

import com.vslearn.dto.AIResponseDTO;
import com.vslearn.dto.VideoProcessingDTO;
import com.vslearn.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

@Service
public class AIServiceImpl implements AIService {
    
    private final String AI_SERVICE_URL = "http://localhost:8001";
    private final RestTemplate restTemplate;
    private final Storage storage;
    private final String bucketName;
    
    @Autowired
    public AIServiceImpl(RestTemplate restTemplate, Storage storage, @Value("${gcp.storage.bucket.name}") String bucketName) {
        this.restTemplate = restTemplate;
        this.storage = storage;
        this.bucketName = bucketName;
    }
    
    @Override
    public VideoProcessingDTO processVideo(MultipartFile videoFile, String expectedWord, String category, String difficulty) {
        try {
            // Upload video to Google Cloud Storage first
            String fileName = System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
            String objectName = "ai-processing-videos/" + fileName;
            
            // Upload to GCS
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(videoFile.getContentType())
                .setMetadata(Map.of(
                    "originalName", videoFile.getOriginalFilename(),
                    "uploadedAt", Instant.now().toString(),
                    "fileSize", String.valueOf(videoFile.getSize()),
                    "expectedWord", expectedWord,
                    "category", category,
                    "difficulty", difficulty
                ))
                .build();
            
            storage.create(blobInfo, videoFile.getBytes());
            
            // Generate signed URL for AI service
            java.net.URL signedUrl = storage.signUrl(blobInfo, 2, java.util.concurrent.TimeUnit.HOURS, 
                Storage.SignUrlOption.withV4Signature());
            
            // Tạo request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Tạo request body với signed URL
            Map<String, Object> requestBody = Map.of(
                "videoUrl", signedUrl.toString(),
                "expectedWord", expectedWord,
                "category", category,
                "difficulty", difficulty,
                "objectName", objectName
            );
            
            // Tạo HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Gọi AI service với video URL thay vì file
            ResponseEntity<Map> response = restTemplate.postForEntity(
                AI_SERVICE_URL + "/process-video-url",
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
    
    /**
     * Xóa video từ GCS sau khi xử lý xong
     */
    public void deleteVideoFromGCS(String objectName) {
        try {
            BlobId blobId = BlobId.of(bucketName, objectName);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                System.out.println("✅ Deleted video from GCS: " + objectName);
            } else {
                System.out.println("⚠️ Video not found in GCS: " + objectName);
            }
        } catch (Exception e) {
            System.err.println("❌ Error deleting video from GCS: " + e.getMessage());
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
            System.out.println("🔍 Checking AI service health at: " + AI_SERVICE_URL + "/health");
            System.out.println("🔍 Using RestTemplate: " + (restTemplate != null ? "available" : "null"));
            
            ResponseEntity<Map> response = restTemplate.getForEntity(
                AI_SERVICE_URL + "/health",
                Map.class
            );
            
            System.out.println("✅ AI service response status: " + response.getStatusCode());
            Map<String, Object> responseBody = response.getBody();
            System.out.println("✅ AI service response body: " + responseBody);
            
            boolean isHealthy = "healthy".equals(responseBody.get("status"));
            System.out.println("✅ AI service is healthy: " + isHealthy);
            return isHealthy;
            
        } catch (Exception e) {
            System.err.println("❌ AI service health check failed: " + e.getMessage());
            System.err.println("❌ Exception type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }
} 