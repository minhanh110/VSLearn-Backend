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
    
    @NotNull(message = "Chủ đề không được để trống")
    private Long topicId;
    
    @NotNull(message = "SubTopic không được để trống")
    private Long subTopicId;
    
    private String description;
    private String videoLink;
    private String region;

    // Thêm trường meaning
    private String meaning;
    private String status;
} 