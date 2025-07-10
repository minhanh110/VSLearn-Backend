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
public class TopicUpdateRequest {
    @NotBlank(message = "Tên chủ đề không được để trống")
    private String topicName;
    
    private Boolean isFree;
    private String status;
    private Long sortOrder;
} 