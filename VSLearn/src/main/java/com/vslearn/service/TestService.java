package com.vslearn.service;

import com.vslearn.dto.request.TestSubmissionRequest;
import com.vslearn.dto.response.TestQuestionResponseDTO;
import com.vslearn.dto.response.TestSubmissionResponseDTO;
import com.vslearn.dto.response.ResponseData;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface TestService {
    ResponseEntity<ResponseData<List<TestQuestionResponseDTO>>> generateTest(Long userId, Long topicId);
    ResponseEntity<ResponseData<TestSubmissionResponseDTO>> submitTest(TestSubmissionRequest request);
    ResponseEntity<ResponseData<TestSubmissionResponseDTO.NextTopicInfo>> getNextTopic(Long userId, Long currentTopicId);
    ResponseEntity<ResponseData<TestSubmissionResponseDTO.NextSubtopicInfo>> getNextSubtopic(Long topicId);
    ResponseEntity<ResponseData<String>> getTopicName(Long topicId);
} 