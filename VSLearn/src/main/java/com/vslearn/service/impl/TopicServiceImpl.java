package com.vslearn.service.impl;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.request.SubTopicRequest;
import com.vslearn.dto.request.NotificationCreateRequest;
import com.vslearn.dto.response.ReviewHistoryEntry;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.dto.response.SubTopicDetailResponse;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.entities.Topic;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.Vocab;
import com.vslearn.entities.User;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.TopicService;
import com.vslearn.service.NotificationService;
import com.vslearn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.nimbusds.jwt.JWTClaimsSet;
import com.vslearn.utils.JwtUtil;

@Service
public class TopicServiceImpl implements TopicService {
    
    private final TopicRepository topicRepository;
    private final SubTopicRepository subTopicRepository;
    private final VocabRepository vocabRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public TopicServiceImpl(TopicRepository topicRepository, SubTopicRepository subTopicRepository, VocabRepository vocabRepository, NotificationService notificationService, UserService userService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.topicRepository = topicRepository;
        this.subTopicRepository = subTopicRepository;
        this.vocabRepository = vocabRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // Helper method to get current user ID from security context
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String email = authentication.getName();
            System.out.println("Getting user ID for email: " + email);
            
            // Get user ID from database by email
            try {
                User user = userRepository.findByUserEmail(email).orElse(null);
                if (user != null) {
                    System.out.println("Found user in database: " + user.getId() + " for email: " + email);
                    return user.getId();
                } else {
                    System.err.println("User not found in database for email: " + email);
                    return 1L; // Fallback
                }
            } catch (Exception e) {
                System.err.println("Failed to get user ID for email " + email + ": " + e.getMessage());
                return 1L; // Default fallback
            }
        }
        return 1L; // Default fallback
    }

    // Helper method to get Content Approver IDs
    private List<Long> getContentApproverIds() {
        try {
            List<Map<String, Object>> contentApprovers = userService.getContentApprovers();
            System.out.println("Found " + contentApprovers.size() + " content approvers: " + contentApprovers);
            return contentApprovers.stream()
                    .map(approver -> (Long) approver.get("id"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to hardcoded IDs if service fails
            System.err.println("Failed to get content approvers from service: " + e.getMessage());
            return List.of(2L, 3L); // Fallback IDs
        }
    }

    // Helper method to notify Content Approvers
    private void notifyContentApprovers(String content, Long fromUserId) {
        List<Long> approverIds = getContentApproverIds();
        System.out.println("Sending notifications to " + approverIds.size() + " approvers: " + approverIds);
        System.out.println("Notification content: " + content);
        System.out.println("From user ID: " + fromUserId);
        
        for (Long approverId : approverIds) {
            try {
                notificationService.createNotification(NotificationCreateRequest.builder()
                        .content(content)
                        .fromUserId(fromUserId != null ? fromUserId : 1L)
                        .toUserId(approverId)
                        .build());
                System.out.println("Successfully sent notification to approver " + approverId);
            } catch (Exception e) {
                // Log error but don't fail the main operation
                System.err.println("Failed to send notification to approver " + approverId + ": " + e.getMessage());
            }
        }
    }

    // Helper method to check if user can modify topic
    private boolean canModifyTopic(Topic topic, String userRole) {
        if (userRole == null) return false;
        
        // General manager and content approver can modify all topics
        if ("ROLE_GENERAL_MANAGER".equals(userRole) || "ROLE_CONTENT_APPROVER".equals(userRole)) {
            return true;
        }
        
        // Content creator can only modify their own topics
        if ("ROLE_CONTENT_CREATOR".equals(userRole)) {
            Long currentUserId = getCurrentUserId();
            return currentUserId != null && currentUserId.equals(topic.getCreatedBy());
        }
        
        return false;
    }

    // Helper method to get current user role
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().iterator().next().getAuthority();
        }
        return null;
    }
    
    @Override
    public TopicListResponse getTopicList(Pageable pageable, String search, String status, Long createdBy) {
        Page<Topic> topicPage;
        if (createdBy != null) {
            if (status != null && !status.trim().isEmpty()) {
                topicPage = topicRepository.findByStatusAndCreatedByAndDeletedAtIsNull(status, createdBy, pageable);
            } else {
                topicPage = topicRepository.findByCreatedByAndDeletedAtIsNull(createdBy, pageable);
            }
        } else if (status != null && !status.trim().isEmpty()) {
            // If requesting a particular status, prefer ordering by sortOrder for active lists
            // Exclude child topics (curriculum change requests) from normal topic lists
            if ("active".equalsIgnoreCase(status)) {
                topicPage = topicRepository.findByParentIsNullAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(status, pageable);
            } else {
                topicPage = topicRepository.findByParentIsNullAndStatusAndDeletedAtIsNull(status, pageable);
            }
        } else if (search != null && !search.trim().isEmpty()) {
            topicPage = topicRepository.findByParentIsNullAndTopicNameContainingIgnoreCaseAndDeletedAtIsNull(search, pageable);
        } else {
            topicPage = topicRepository.findByParentIsNullAndDeletedAtIsNull(pageable);
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
    public void reorderTopics(List<Map<String, Object>> items) {
        // Expecting list of { id, sortOrder }
        for (Map<String, Object> item : items) {
            Long id = Long.parseLong(item.get("id").toString());
            Long sortOrder = Long.parseLong(item.get("sortOrder").toString());
            Topic topic = topicRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Topic not found with id: " + id));
            topic.setSortOrder(sortOrder);
            topic.setUpdatedAt(Instant.now());
            Long currentUserId = getCurrentUserId();
            topic.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
            topicRepository.save(topic);
        }
        
        // Create child topic for curriculum change approval
        createChildTopicForCurriculumChange(items);
    }

    @Override
    public TopicDetailResponse getTopicDetail(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        List<SubTopic> subTopics = subTopicRepository.findByTopic_Id(topicId);
        List<SubTopicDetailResponse> subtopicResponses = subTopics.stream().map(sub -> {
            List<Vocab> vocabs = vocabRepository.findBySubTopic_Id(sub.getId());
            List<VocabDetailResponse> vocabResponses = vocabs.stream()
                .map(this::convertToVocabDetailResponse)
                .collect(Collectors.toList());
            return SubTopicDetailResponse.builder()
                .id(sub.getId())
                .subTopicName(sub.getSubTopicName())
                .sortOrder(sub.getSortOrder())
                .vocabs(vocabResponses)
                .build();
        }).collect(Collectors.toList());
        return TopicDetailResponse.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .isFree(topic.getIsFree())
                .status(topic.getStatus())
                .sortOrder(topic.getSortOrder())
                .subtopics(subtopicResponses)
                .createdAt(topic.getCreatedAt())
                .createdBy(topic.getCreatedBy())
                .updatedAt(topic.getUpdatedAt())
                .updatedBy(topic.getUpdatedBy())
                .deletedAt(topic.getDeletedAt())
                .deletedBy(topic.getDeletedBy())
                .build();
    }
    
    @Override
    public TopicDetailResponse createTopic(TopicCreateRequest request) {
        Topic topic = Topic.builder()
                .topicName(request.getTopicName())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .status("pending")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0L)
                .createdAt(Instant.now())
                .createdBy(1L)
                .build();
        Topic savedTopic = topicRepository.save(topic);
        // Tạo subtopics và vocab lồng nhau
        if (request.getSubtopics() != null && !request.getSubtopics().isEmpty()) {
            for (SubTopicRequest subReq : request.getSubtopics()) {
                if (subReq.getSubTopicName() != null && !subReq.getSubTopicName().trim().isEmpty()) {
                    SubTopic subtopic = SubTopic.builder()
                            .topic(savedTopic)
                            .subTopicName(subReq.getSubTopicName().trim())
                            .status("pending")
                            .sortOrder(subReq.getSortOrder() != null ? subReq.getSortOrder() : 0L)
                            .createdAt(Instant.now())
                            .createdBy(1L)
                            .build();
                    SubTopic savedSub = subTopicRepository.save(subtopic);
                    // TODO: Tạo vocab cho subtopic này nếu có (cần repo và entity cho vocab)
                    // for (VocabRequest v : subReq.getVocabs()) { ... }
                }
            }
        }
        
        // Thông báo cho Content Approver về topic mới cần duyệt
        Long currentUserId = getCurrentUserId();
        String content = String.format("Chủ đề mới \"%s\" cần được duyệt. Xem chi tiết [topic:%d]", savedTopic.getTopicName(), savedTopic.getId());
        notifyContentApprovers(content, currentUserId);
        
        return convertToTopicDetailResponse(savedTopic);
    }
    
    @Override
    public TopicDetailResponse updateTopic(Long topicId, TopicUpdateRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        // Kiểm tra quyền chỉnh sửa
        String userRole = getCurrentUserRole();
        if (!canModifyTopic(topic, userRole)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa chủ đề này");
        }
        
        topic.setTopicName(request.getTopicName());
        topic.setIsFree(request.getIsFree() != null ? request.getIsFree() : topic.getIsFree());
        // Content Creator update topic thì status luôn chuyển về pending để chờ duyệt lại
        topic.setStatus("pending");
        topic.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : topic.getSortOrder());
        topic.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        topic.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        
        Topic updatedTopic = topicRepository.save(topic);
        
        // Thông báo cho Content Approver về topic đã được chỉnh sửa và cần duyệt lại
        String content = String.format("Chủ đề \"%s\" đã được chỉnh sửa và cần duyệt lại. Xem chi tiết [topic:%d]", updatedTopic.getTopicName(), updatedTopic.getId());
        notifyContentApprovers(content, currentUserId);
        
        return convertToTopicDetailResponse(updatedTopic);
    }
    
    @Override
    public TopicDetailResponse updateTopicStatus(Long topicId, String newStatus) {
        System.out.println("🔍 updateTopicStatus called - topicId: " + topicId + ", newStatus: " + newStatus);
        
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        System.out.println("🔍 Found topic - current status: " + topic.getStatus() + ", name: " + topic.getTopicName());
        
        topic.setStatus(newStatus);
        topic.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        topic.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        
        Topic updatedTopic = topicRepository.save(topic);
        
        System.out.println("🔍 Topic saved - new status: " + updatedTopic.getStatus());
        
        // Thông báo cho Content Creator về kết quả duyệt/từ chối
        String content;
        if ("active".equals(newStatus)) {
            content = String.format("Chủ đề \"%s\" đã được phê duyệt và đang hoạt động. Xem chi tiết [topic:%d]", updatedTopic.getTopicName(), updatedTopic.getId());
        } else if ("rejected".equals(newStatus)) {
            content = String.format("Chủ đề \"%s\" đã bị từ chối. Chỉnh sửa lại [topic:%d]", updatedTopic.getTopicName(), updatedTopic.getId());
        } else {
            // Không gửi thông báo cho các trạng thái khác
            return convertToTopicDetailResponse(updatedTopic);
        }
        
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(updatedTopic.getCreatedBy())
                .build());
        
        return convertToTopicDetailResponse(updatedTopic);
    }
    
    @Override
    public void disableTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        // Kiểm tra quyền xóa
        String userRole = getCurrentUserRole();
        if (!canModifyTopic(topic, userRole)) {
            throw new RuntimeException("Bạn không có quyền xóa chủ đề này");
        }
        
        topic.setDeletedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        topic.setDeletedBy(currentUserId != null ? currentUserId : 1L);
        
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

    private SubTopicDetailResponse convertToSubTopicDetailResponse(SubTopic subtopic) {
        List<Vocab> vocabs = vocabRepository.findBySubTopic_Id(subtopic.getId());
        List<VocabDetailResponse> vocabResponses = vocabs.stream()
            .map(this::convertToVocabDetailResponse)
            .collect(Collectors.toList());
        return SubTopicDetailResponse.builder()
            .id(subtopic.getId())
            .subTopicName(subtopic.getSubTopicName())
            .sortOrder(subtopic.getSortOrder())
            .vocabs(vocabResponses)
            .build();
    }

    private VocabDetailResponse convertToVocabDetailResponse(Vocab vocab) {
        return VocabDetailResponse.builder()
            .id(vocab.getId())
            .vocab(vocab.getVocab())
            .createdAt(vocab.getCreatedAt())
            .createdBy(vocab.getCreatedBy())
            .updatedAt(vocab.getUpdatedAt())
            .updatedBy(vocab.getUpdatedBy())
            .deletedAt(vocab.getDeletedAt())
            .deletedBy(vocab.getDeletedBy())
            .build();
    }

    // Helpers for update workflow
    private Topic getParentTopicOrThrow(Long parentTopicId) {
        return topicRepository.findById(parentTopicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + parentTopicId));
    }

    private Topic getOrCreateChildDraft(Topic parent) {
        List<Topic> children = topicRepository.findByParent_IdAndDeletedAtIsNull(parent.getId());
        if (!children.isEmpty()) {
            return children.get(0);
        }
        Topic child = Topic.builder()
                .topicName(parent.getTopicName())
                .isFree(parent.getIsFree() != null ? parent.getIsFree() : false)
                .status("draft")
                .sortOrder(parent.getSortOrder())
                .createdAt(Instant.now())
                .createdBy(parent.getCreatedBy())
                .parent(parent)
                .build();
        return topicRepository.save(child);
    }

    @Override
    public TopicDetailResponse requestUpdate(Long parentTopicId, Long assigneeUserId, String message) {
        Topic parent = getParentTopicOrThrow(parentTopicId);
        // Only approver/GM should call this (checked at controller). Set parent to request_update
        parent.setStatus("request_update");
        parent.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        parent.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        topicRepository.save(parent);
        // Ensure a child draft exists
        Topic child = getOrCreateChildDraft(parent);
        // Notify creator or assignee
        Long toUserId = (assigneeUserId != null) ? assigneeUserId : parent.getCreatedBy();
        String content = String.format("Chủ đề \"%s\" cần chỉnh sửa. Chỉnh sửa lại [topic:%d]%s",
                parent.getTopicName(), parent.getId(), (message != null && !message.isBlank() ? ("\nGhi chú: " + message) : ""));
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(toUserId)
                .build());
        
        // Also notify content approvers about the request update
        String approverContent = String.format("Có yêu cầu chỉnh sửa cho chủ đề \"%s\". Xem chi tiết [topic:%d]", parent.getTopicName(), parent.getId());
        notifyContentApprovers(approverContent, currentUserId);
        return convertToTopicDetailResponse(child);
    }

    @Override
    public TopicDetailResponse saveDraft(Long parentTopicId, TopicUpdateRequest request) {
        Topic parent = getParentTopicOrThrow(parentTopicId);
        // Creator can only modify own topics
        if (!canModifyTopic(parent, getCurrentUserRole())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa chủ đề này");
        }
        // Ensure child draft exists
        Topic child = getOrCreateChildDraft(parent);
        // Update draft fields
        if (request.getTopicName() != null && !request.getTopicName().trim().isEmpty()) {
            child.setTopicName(request.getTopicName().trim());
        }
        if (request.getIsFree() != null) {
            child.setIsFree(request.getIsFree());
        }
        if (request.getSortOrder() != null) {
            child.setSortOrder(request.getSortOrder());
        }
        child.setStatus("draft");
        child.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        child.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        Topic saved = topicRepository.save(child);
        // NOTE: Minimal implementation does not clone/update subtopics/vocabs here
        return convertToTopicDetailResponse(saved);
    }

    @Override
    public TopicDetailResponse submitUpdate(Long parentTopicId) {
        Topic parent = getParentTopicOrThrow(parentTopicId);
        if (!canModifyTopic(parent, getCurrentUserRole())) {
            throw new RuntimeException("Bạn không có quyền gửi duyệt chủ đề này");
        }
        List<Topic> children = topicRepository.findByParent_IdAndDeletedAtIsNull(parent.getId());
        if (children.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bản nháp cập nhật cho chủ đề này");
        }
        Topic child = children.get(0);
        child.setStatus("pending");
        child.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        child.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        Topic saved = topicRepository.save(child);
        // Notify creator about submission
        String content = String.format("Đã gửi cập nhật cho chủ đề \"%s\". Xem chi tiết [topic:%d]", parent.getTopicName(), parent.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(parent.getCreatedBy())
                .build());
        
        // Also notify content approvers about the submitted update
        String approverContent = String.format("Có cập nhật chủ đề \"%s\" cần duyệt. Xem chi tiết [topic:%d]", parent.getTopicName(), parent.getId());
        notifyContentApprovers(approverContent, currentUserId);
        return convertToTopicDetailResponse(saved);
    }

    @Override
    public TopicDetailResponse approveUpdate(Long parentTopicId) {
        Topic parent = getParentTopicOrThrow(parentTopicId);
        // Approver/GM only (checked at controller)
        List<Topic> children = topicRepository.findByParent_IdAndDeletedAtIsNull(parent.getId());
        if (children.isEmpty()) {
            throw new RuntimeException("Không có bản cập nhật nào để phê duyệt");
        }
        Topic child = children.get(0);
        if (!"pending".equalsIgnoreCase(child.getStatus())) {
            throw new RuntimeException("Chỉ phê duyệt được bản cập nhật ở trạng thái pending");
        }
        // Merge minimal fields back to parent
        parent.setTopicName(child.getTopicName());
        parent.setIsFree(child.getIsFree());
        parent.setSortOrder(child.getSortOrder());
        parent.setStatus("active");
        parent.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        parent.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        Topic updatedParent = topicRepository.save(parent);
        // Soft-delete child
        child.setDeletedAt(Instant.now());
        child.setDeletedBy(currentUserId != null ? currentUserId : 1L);
        topicRepository.save(child);
        // Notify creator approved
        String content = String.format("Cập nhật chủ đề \"%s\" đã được phê duyệt. Xem chi tiết [topic:%d]", parent.getTopicName(), parent.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(parent.getCreatedBy())
                .build());
        return convertToTopicDetailResponse(updatedParent);
    }

    @Override
    public TopicDetailResponse rejectUpdate(Long parentTopicId) {
        Topic parent = getParentTopicOrThrow(parentTopicId);
        // Approver/GM only (checked at controller)
        List<Topic> children = topicRepository.findByParent_IdAndDeletedAtIsNull(parent.getId());
        if (children.isEmpty()) {
            throw new RuntimeException("Không có bản cập nhật nào để từ chối");
        }
        Topic child = children.get(0);
        // Move child back to draft for further edits
        child.setStatus("draft");
        child.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        child.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        topicRepository.save(child);
        // Keep parent in request_update until approved
        parent.setStatus("request_update");
        parent.setUpdatedAt(Instant.now());
        parent.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        Topic savedParent = topicRepository.save(parent);
        // Notify creator rejected
        String content = String.format("Cập nhật chủ đề \"%s\" bị từ chối. Chỉnh sửa lại [topic:%d]", parent.getTopicName(), parent.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(parent.getCreatedBy())
                .build());
        return convertToTopicDetailResponse(savedParent);
    }

    @Override
    public TopicDetailResponse createDraftTopic(TopicCreateRequest request) {
        Topic topic = Topic.builder()
                .topicName(request.getTopicName())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .status("draft")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0L)
                .createdAt(Instant.now())
                .createdBy(1L)
                .build();
        Topic savedTopic = topicRepository.save(topic);
        if (request.getSubtopics() != null && !request.getSubtopics().isEmpty()) {
            for (SubTopicRequest subReq : request.getSubtopics()) {
                if (subReq.getSubTopicName() != null && !subReq.getSubTopicName().trim().isEmpty()) {
                    SubTopic subtopic = SubTopic.builder()
                            .topic(savedTopic)
                            .subTopicName(subReq.getSubTopicName().trim())
                            .status("draft")
                            .sortOrder(subReq.getSortOrder() != null ? subReq.getSortOrder() : 0L)
                            .createdAt(Instant.now())
                            .createdBy(1L)
                            .build();
                    subTopicRepository.save(subtopic);
                }
            }
        }
        return convertToTopicDetailResponse(savedTopic);
    }

    @Override
    public TopicDetailResponse createChildTopicForCurriculumChange(List<Map<String, Object>> items) {
        // Get the first topic as parent (assuming all topics are from the same curriculum)
        Long parentTopicId = null;
        if (!items.isEmpty()) {
            parentTopicId = Long.parseLong(items.get(0).get("id").toString());
        }
        
        // Get parent topic
        Topic parentTopic = null;
        if (parentTopicId != null) {
            parentTopic = topicRepository.findById(parentTopicId).orElse(null);
        }
        
        // Create a child topic representing the curriculum change
        Topic childTopic = Topic.builder()
                .topicName("Lộ trình đề xuất - " + Instant.now().toString().substring(0, 10))
                .isFree(false) // Will be updated based on actual changes
                .status("pending")
                .sortOrder(0L)
                .parent(parentTopic) // Set parent topic
                .createdAt(Instant.now())
                .createdBy(getCurrentUserId() != null ? getCurrentUserId() : 1L)
                .build();
        
        Topic savedChildTopic = topicRepository.save(childTopic);
        
        // Notify content approvers about the curriculum change request
        Long currentUserId = getCurrentUserId();
        String content = String.format("Có yêu cầu thay đổi lộ trình học mới. Xem chi tiết [curriculum:%d]", savedChildTopic.getId());
        notifyContentApprovers(content, currentUserId);
        
        return convertToTopicDetailResponse(savedChildTopic);
    }

    @Override
    public List<TopicDetailResponse> getCurriculumRequests() {
        // Get only child topics (curriculum change requests) with pending status
        List<Topic> pendingChildTopics = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNull("pending");
        return pendingChildTopics.stream()
                .map(this::convertToTopicDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TopicDetailResponse approveCurriculumRequest(Long childTopicId) {
        Topic childTopic = topicRepository.findById(childTopicId)
                .orElseThrow(() -> new RuntimeException("Child topic not found with id: " + childTopicId));
        
        if (!"pending".equalsIgnoreCase(childTopic.getStatus())) {
            throw new RuntimeException("Chỉ phê duyệt được yêu cầu ở trạng thái pending");
        }
        
        // Apply the curriculum changes (in this case, just approve the child topic)
        childTopic.setStatus("active");
        childTopic.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        childTopic.setUpdatedBy(currentUserId != null ? currentUserId : 1L);
        
        Topic savedChildTopic = topicRepository.save(childTopic);
        
        // Notify creator that curriculum change was approved
        String content = String.format("Yêu cầu thay đổi lộ trình học đã được phê duyệt.");
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(childTopic.getCreatedBy())
                .build());
        
        return convertToTopicDetailResponse(savedChildTopic);
    }

    @Override
    public TopicDetailResponse rejectCurriculumRequest(Long childTopicId, String reason) {
        Topic childTopic = topicRepository.findById(childTopicId)
                .orElseThrow(() -> new RuntimeException("Child topic not found with id: " + childTopicId));
        
        // Soft delete the child topic
        childTopic.setDeletedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        childTopic.setDeletedBy(currentUserId != null ? currentUserId : 1L);
        
        Topic savedChildTopic = topicRepository.save(childTopic);
        
        // Notify creator that curriculum change was rejected
        String content = String.format("Yêu cầu thay đổi lộ trình học bị từ chối. Lý do: %s", reason != null ? reason : "Không có lý do");
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId != null ? currentUserId : 1L)
                .toUserId(childTopic.getCreatedBy())
                .build());
        
        return convertToTopicDetailResponse(savedChildTopic);
    }

    @Override
    public List<ReviewHistoryEntry> getTopicReviewHistory(Long topicId) {
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            return new ArrayList<>();
        }
        
        List<ReviewHistoryEntry> history = new ArrayList<>();
        
        // 1. Tạo topic
        User creator = userRepository.findById(topic.getCreatedBy()).orElse(null);
        String creatorName = creator != null ? 
            (creator.getFirstName() != null ? creator.getFirstName() : "") + 
            (creator.getLastName() != null ? " " + creator.getLastName() : "") : 
            "Người dùng";
        if (creatorName.trim().isEmpty()) {
            creatorName = "Người dùng";
        }
        
        history.add(ReviewHistoryEntry.builder()
            .id(1L)
            .action("created")
            .date(topic.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
            .actor(creatorName)
            .build());
        
        // 2. Cập nhật/duyệt topic
        if (topic.getUpdatedAt() != null && topic.getUpdatedBy() != null) {
            User updater = userRepository.findById(topic.getUpdatedBy()).orElse(null);
            String updaterName = updater != null ? 
                (updater.getFirstName() != null ? updater.getFirstName() : "") + 
                (updater.getLastName() != null ? " " + updater.getLastName() : "") : 
                "Người kiểm duyệt";
            if (updaterName.trim().isEmpty()) {
                updaterName = "Người kiểm duyệt";
            }
            
            String action = "updated";
            if ("active".equalsIgnoreCase(topic.getStatus())) {
                action = "approved";
            } else if ("rejected".equalsIgnoreCase(topic.getStatus())) {
                action = "rejected";
            }
            
            history.add(ReviewHistoryEntry.builder()
                .id(2L)
                .action(action)
                .date(topic.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .actor(updaterName)
                .build());
        }
        
        return history;
    }
} 