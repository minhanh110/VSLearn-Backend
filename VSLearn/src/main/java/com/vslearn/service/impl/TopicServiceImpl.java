package com.vslearn.service.impl;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.entities.Topic;
import com.vslearn.repository.TopicRepository;
import com.vslearn.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl implements TopicService {
    private final TopicRepository topicRepository;

    @Autowired
    public TopicServiceImpl(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public TopicListResponse getTopicList(Pageable pageable, String search) {
        Page<Topic> topicPage;
        
        if (search != null && !search.trim().isEmpty()) {
            topicPage = topicRepository.findByTopicNameContainingIgnoreCaseAndDeletedAtIsNull(search, pageable);
        } else {
            topicPage = topicRepository.findByDeletedAtIsNull(pageable);
        }
        
        List<TopicDetailResponse> topicList = topicPage.getContent().stream()
                .map(this::convertToTopicDetailResponse)
                .collect(Collectors.toList());
        
        return TopicListResponse.builder()
                .topicList(topicList)
                .currentPage(topicPage.getNumber())
                .totalPages(topicPage.getTotalPages())
                .totalElements(topicPage.getTotalElements())
                .pageSize(topicPage.getSize())
                .hasNext(topicPage.hasNext())
                .hasPrevious(topicPage.hasPrevious())
                .build();
    }

    @Override
    public TopicDetailResponse getTopicDetail(Long topicId) {
        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            throw new RuntimeException("Không tìm thấy chủ đề với ID: " + topicId);
        }
        return convertToTopicDetailResponse(topic.get());
    }

    @Override
    public TopicDetailResponse createTopic(TopicCreateRequest request) {
        Topic topic = Topic.builder()
                .topicName(request.getTopicName())
                .isFree(request.getIsFree())
                .status(request.getStatus())
                .sortOrder(request.getSortOrder())
                .createdAt(Instant.now())
                .createdBy(1L) // TODO: Get from current user
                .build();
        
        Topic savedTopic = topicRepository.save(topic);
        return convertToTopicDetailResponse(savedTopic);
    }

    @Override
    public TopicDetailResponse updateTopic(Long topicId, TopicUpdateRequest request) {
        Optional<Topic> existingTopic = topicRepository.findById(topicId);
        if (existingTopic.isEmpty()) {
            throw new RuntimeException("Không tìm thấy chủ đề với ID: " + topicId);
        }
        
        Topic topic = existingTopic.get();
        topic.setTopicName(request.getTopicName());
        if (request.getIsFree() != null) {
            topic.setIsFree(request.getIsFree());
        }
        if (request.getStatus() != null) {
            topic.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            topic.setSortOrder(request.getSortOrder());
        }
        topic.setUpdatedAt(Instant.now());
        topic.setUpdatedBy(1L); // TODO: Get from current user
        
        Topic savedTopic = topicRepository.save(topic);
        return convertToTopicDetailResponse(savedTopic);
    }

    @Override
    public void disableTopic(Long topicId) {
        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            throw new RuntimeException("Không tìm thấy chủ đề với ID: " + topicId);
        }
        
        Topic topicToDisable = topic.get();
        topicToDisable.setDeletedAt(Instant.now());
        topicToDisable.setDeletedBy(1L); // TODO: Get from current user
        
        topicRepository.save(topicToDisable);
    }

    @Override
    public List<TopicDetailResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findByDeletedAtIsNull();
        return topics.stream()
                .map(this::convertToTopicDetailResponse)
                .collect(Collectors.toList());
    }

    private TopicDetailResponse convertToTopicDetailResponse(Topic topic) {
        return TopicDetailResponse.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .isFree(topic.getIsFree())
                .status(topic.getStatus())
                .sortOrder(topic.getSortOrder())
                .createdAt(topic.getCreatedAt())
                .createdBy(topic.getCreatedBy())
                .updatedAt(topic.getUpdatedAt())
                .updatedBy(topic.getUpdatedBy())
                .deletedAt(topic.getDeletedAt())
                .deletedBy(topic.getDeletedBy())
                .build();
    }
} 