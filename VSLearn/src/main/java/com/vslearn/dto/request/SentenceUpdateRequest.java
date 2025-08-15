package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceUpdateRequest {
    
    private String sentenceVideo;
    
    private String sentenceMeaning;
    
    private String sentenceDescription;
    
    private Long topicId;
    
    private List<Long> vocabIds; // Danh sách vocab IDs để cập nhật sentence_vocab relationships
    
    private Long parentId;
    
    private String status;
} 