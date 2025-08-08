package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadResponse {
    private String videoUrl;
    private String fileName;
    private String objectName; // GCS object name
    private Long fileSize;
    private String contentType;
    private String duration; // Video duration (nếu có)
    private String thumbnailUrl; // Thumbnail URL (nếu generate được)
    private Map<String, Object> metadata;
} 