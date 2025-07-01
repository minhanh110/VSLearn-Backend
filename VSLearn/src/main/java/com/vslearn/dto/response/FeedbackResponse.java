package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    
    private Long id;
    private Long topicId;
    private String topicName;
    private Integer rating;
    private String feedbackContent;
    private Double totalPoint;
    private String createdBy;
    private Instant createdAt;
    private String message;
} 