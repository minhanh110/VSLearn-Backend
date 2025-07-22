package com.vslearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabCreateRequest {
    @NotBlank(message = "Tên từ vựng không được để trống")
    private String vocab;
    
    private Long topicId;
    
    private Long subTopicId;
    
    private String description;
    private String videoLink;
    private String region;

    // Thêm trường meaning
    private String meaning;
    // Status sẽ được set tự động thành "pending" cho Content Creator
} 