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
public class VideoMetadata {
    private String fileName;
    private Long fileSize;
    private String duration;
    private String thumbnailUrl;
    private Instant uploadedAt;
    private String contentType;
    private String objectName; // GCS object name
} 