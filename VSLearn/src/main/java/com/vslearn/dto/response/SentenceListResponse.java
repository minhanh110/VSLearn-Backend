package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceListResponse {
    
    private List<SentenceDetailResponse> sentenceList;
    
    private long totalElements;
    
    private int totalPages;
    
    private int currentPage;
    
    private int pageSize;
} 