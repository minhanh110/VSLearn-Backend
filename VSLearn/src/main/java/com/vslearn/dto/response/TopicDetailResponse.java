package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicDetailResponse {
    private Long id;
    private String topicName;
    private Boolean isFree;
    private String status;
    private Long sortOrder;
    private Long subtopicCount; // Number of subtopics for this topic
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
    private Instant deletedAt;
    private Long deletedBy;
    private List<SubTopicDetailResponse> subtopics;
} 