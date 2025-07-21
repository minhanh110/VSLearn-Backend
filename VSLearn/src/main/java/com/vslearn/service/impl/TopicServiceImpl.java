package com.vslearn.service.impl;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.entities.Topic;
import com.vslearn.entities.SubTopic;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicServiceImpl implements TopicService {
    
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;
    
    @Autowired
    public TopicServiceImpl(TopicRepository topicRepository, SubTopicRepository subTopicRepository) {
        this.topicRepository = topicRepository;
        this.subTopicRepository = subTopicRepository;
    }
    
    @Override
    public TopicListResponse getTopicList(Pageable pageable, String search, String status) {
        Page<Topic> topicPage;
        
        if (status != null && !status.trim().isEmpty()) {
            topicPage = topicRepository.findByStatusAndDeletedAtIsNull(status, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
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
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        return convertToTopicDetailResponse(topic);
    }
    
    @Override
    public TopicDetailResponse createTopic(TopicCreateRequest request) {
        Topic topic = Topic.builder()
                .topicName(request.getTopicName())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .status("pending") // Content Creator tạo topic luôn có status pending
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0L)
                .createdAt(Instant.now())
                .createdBy(1L) // TODO: Get from security context
                .build();
        
        Topic savedTopic = topicRepository.save(topic);
        
        // Tạo subtopics nếu có
        if (request.getSubtopics() != null && !request.getSubtopics().isEmpty()) {
            for (String subtopicName : request.getSubtopics()) {
                if (subtopicName != null && !subtopicName.trim().isEmpty()) {
                    SubTopic subtopic = SubTopic.builder()
                            .topic(savedTopic)
                            .subTopicName(subtopicName.trim())
                            .status("pending") // Subtopics cũng cần pending
                            .sortOrder(0L)
                            .createdAt(Instant.now())
                            .createdBy(1L) // TODO: Get from security context
                            .build();
                    subTopicRepository.save(subtopic);
                }
            }
        }
        
        return convertToTopicDetailResponse(savedTopic);
    }
    
    @Override
    public TopicDetailResponse updateTopic(Long topicId, TopicUpdateRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        topic.setTopicName(request.getTopicName());
        topic.setIsFree(request.getIsFree() != null ? request.getIsFree() : topic.getIsFree());
        // Content Creator update topic thì status luôn chuyển về pending để chờ duyệt lại
        topic.setStatus("pending");
        topic.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : topic.getSortOrder());
        topic.setUpdatedAt(Instant.now());
        topic.setUpdatedBy(1L); // TODO: Get from security context
        
        Topic updatedTopic = topicRepository.save(topic);
        return convertToTopicDetailResponse(updatedTopic);
    }
    
    @Override
    public TopicDetailResponse updateTopicStatus(Long topicId, String newStatus) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        topic.setStatus(newStatus);
        topic.setUpdatedAt(Instant.now());
        topic.setUpdatedBy(1L); // TODO: Get from security context
        
        Topic updatedTopic = topicRepository.save(topic);
        return convertToTopicDetailResponse(updatedTopic);
    }
    
    @Override
    public void disableTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        topic.setDeletedAt(Instant.now());
        topic.setDeletedBy(1L); // TODO: Get from security context
        
        topicRepository.save(topic);
    }
    
    @Override
    public List<TopicDetailResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findByDeletedAtIsNull();
        return topics.stream()
                .map(this::convertToTopicDetailResponse)
                .collect(Collectors.toList());
    }
    
    private TopicDetailResponse convertToTopicDetailResponse(Topic topic) {
        // Calculate actual subtopic count
        Long subtopicCount = (long) subTopicRepository.findByTopic_Id(topic.getId()).size();
        
        return TopicDetailResponse.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .isFree(topic.getIsFree())
                .status(topic.getStatus())
                .sortOrder(topic.getSortOrder())
                .subtopicCount(subtopicCount)
                .createdAt(topic.getCreatedAt())
                .createdBy(topic.getCreatedBy())
                .updatedAt(topic.getUpdatedAt())
                .updatedBy(topic.getUpdatedBy())
                .deletedAt(topic.getDeletedAt())
                .deletedBy(topic.getDeletedBy())
                .build();
    }
} 