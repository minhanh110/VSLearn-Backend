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
    // Status sẽ được set tự động thành "pending" cho Content Creator
    private Long sortOrder;
} 