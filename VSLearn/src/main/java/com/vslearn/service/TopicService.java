package com.vslearn.service;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopicService {
    TopicListResponse getTopicList(Pageable pageable, String search);
    TopicDetailResponse getTopicDetail(Long topicId);
    TopicDetailResponse createTopic(TopicCreateRequest request);
    TopicDetailResponse updateTopic(Long topicId, TopicUpdateRequest request);
    void disableTopic(Long topicId);
    List<TopicDetailResponse> getAllTopics();
} 