package com.vslearn.service;

import com.vslearn.dto.request.SentenceCreateRequest;
import com.vslearn.dto.request.SentenceUpdateRequest;
import com.vslearn.dto.response.SentenceDetailResponse;
import com.vslearn.dto.response.SentenceListResponse;
import com.vslearn.dto.response.VideoUploadResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SentenceService {
    
    // CRUD Operations
    SentenceDetailResponse createSentence(SentenceCreateRequest request);
    
    SentenceDetailResponse updateSentence(Long sentenceId, SentenceUpdateRequest request);
    
    SentenceDetailResponse getSentenceDetail(Long sentenceId);
    
    SentenceListResponse getSentenceList(Pageable pageable, String search, String topic, String status, Long createdBy);
    
    Map<String, Object> deleteSentence(Long sentenceId);
    
    // Video Upload
    VideoUploadResponse uploadVideoToGCS(MultipartFile file, String fileName) throws Exception;
    
    // Utility methods
    List<SentenceDetailResponse> getSentencesByTopicId(Long topicId);
    
    boolean existsByTopicId(Long topicId);
} 