package com.vslearn.service;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.response.ReviewHistoryEntry;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TopicService {
    TopicListResponse getTopicList(Pageable pageable, String search, String status, Long createdBy);
    TopicDetailResponse getTopicDetail(Long topicId);
    TopicDetailResponse createTopic(TopicCreateRequest request);
    TopicDetailResponse updateTopic(Long topicId, TopicUpdateRequest request);
    TopicDetailResponse updateTopicStatus(Long topicId, String newStatus);
    void disableTopic(Long topicId);
    List<TopicDetailResponse> getAllTopics();

    // New: reorder topics by sortOrder
    void reorderTopics(List<Map<String, Object>> items);

    // New: update workflow
    TopicDetailResponse requestUpdate(Long parentTopicId, Long assigneeUserId, String message);
    TopicDetailResponse saveDraft(Long parentTopicId, TopicUpdateRequest request);
    TopicDetailResponse submitUpdate(Long parentTopicId);
    TopicDetailResponse approveUpdate(Long parentTopicId);
    TopicDetailResponse rejectUpdate(Long parentTopicId);

    // New: create draft topic
    TopicDetailResponse createDraftTopic(TopicCreateRequest request);

    // New: curriculum change workflow
    TopicDetailResponse createChildTopicForCurriculumChange(List<Map<String, Object>> items);
    List<TopicDetailResponse> getCurriculumRequests();
    TopicDetailResponse approveCurriculumRequest(Long childTopicId);
    TopicDetailResponse rejectCurriculumRequest(Long childTopicId, String reason);

    // New: curriculum preview workflow (Option 1 - Parent-Child)
    void createCurriculumPreview(List<Map<String, Object>> items);
    List<TopicDetailResponse> getCurriculumPreviews();
    void approveCurriculumPreview();
    void rejectCurriculumPreview(String reason);
    
    // New: topic permission management
    TopicDetailResponse assignTopicPermission(Long topicId, Long assigneeUserId);
    TopicDetailResponse revokeTopicPermission(Long topicId);

    // New: review history
    List<ReviewHistoryEntry> getTopicReviewHistory(Long topicId);
} 