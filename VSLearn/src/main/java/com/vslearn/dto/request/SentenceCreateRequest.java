package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceCreateRequest {
    
    @NotBlank(message = "Video URL không được để trống")
    private String sentenceVideo;
    
    private String sentenceMeaning;
    
    private String sentenceDescription;
    
    @NotNull(message = "Topic ID không được để trống")
    private Long topicId;
    
    private List<Long> vocabIds; // Danh sách vocab IDs để tạo sentence_vocab relationships
    
    private Long parentId; // Optional: parent sentence ID
} 