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
public class SentenceDetailResponse {
    
    private Long id;
    
    private String sentenceVideo;
    
    private String sentenceMeaning;
    
    private String sentenceDescription;
    
    private TopicDetailResponse topic;
    
    private List<VocabDetailResponse> vocabs; // Danh sách vocab trong sentence
    
    private SentenceDetailResponse parent; // Parent sentence nếu có
    
    private Instant createdAt;
    
    private Long createdBy;
    
    private Instant updatedAt;
    
    private Long updatedBy;
} 