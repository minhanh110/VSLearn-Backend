package com.vslearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicCreateRequest {
    @NotBlank(message = "Tên chủ đề không được để trống")
    private String topicName;
    
    private Boolean isFree = false;
    private String status = "active";
    private Long sortOrder = 0L;
} 