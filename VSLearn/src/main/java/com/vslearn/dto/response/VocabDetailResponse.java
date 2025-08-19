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
public class VocabDetailResponse {
    private Long id;
    private String vocab;
    private String topicName;
    private Long topicId; // Thêm topicId
    private String subTopicName;
    private String description;
    private String videoLink;
    private String region;
    private String status;
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
    private Instant deletedAt;
    private Long deletedBy;
} 