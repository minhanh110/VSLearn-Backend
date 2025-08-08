package com.vslearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDTO {
    private String taskId;
    private String status; // "processing", "completed", "failed"
    private Integer progress; // 0-100
    private RecognitionResult result;
    private String error;
    private String createdAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecognitionResult {
        private String predictedWord;
        private Double confidence;
        private List<FrameResult> frameResults;
        private Double overallAccuracy;
        private String feedback;
        private List<String> suggestions;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrameResult {
        private Integer frameNumber;
        private String detectedSign;
        private Double confidence;
        private String timestamp;
    }
} 