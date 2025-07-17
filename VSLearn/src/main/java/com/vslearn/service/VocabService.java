package com.vslearn.service;

import com.vslearn.dto.request.VocabCreateRequest;
import com.vslearn.dto.request.VocabUpdateRequest;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.dto.response.VocabListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface VocabService {
    VocabListResponse getVocabList(Pageable pageable, String search, String topic, String region, String letter, String status);
    VocabDetailResponse getVocabDetail(Long vocabId);
    VocabDetailResponse createVocab(VocabCreateRequest request);
    VocabDetailResponse updateVocab(Long vocabId, VocabUpdateRequest request);
    void disableVocab(Long vocabId);
    VocabListResponse getRejectedVocabList(Pageable pageable);
    List<Map<String, Object>> getTopics();
    List<Map<String, Object>> getRegions();
} 