package com.vslearn.service.impl;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.request.SubTopicRequest;
import com.vslearn.dto.request.VocabCreateRequest;
import com.vslearn.dto.request.NotificationCreateRequest;
import com.vslearn.dto.response.ReviewHistoryEntry;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.dto.response.SubTopicDetailResponse;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.entities.Sentence;
import com.vslearn.entities.Topic;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.Vocab;
import com.vslearn.entities.User;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.SentenceRepository;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final SentenceRepository sentenceRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public TopicServiceImpl(TopicRepository topicRepository, SubTopicRepository subTopicRepository, VocabRepository vocabRepository, SentenceRepository sentenceRepository, NotificationService notificationService, UserService userService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.topicRepository = topicRepository;
        this.subTopicRepository = subTopicRepository;
        this.vocabRepository = vocabRepository;
        this.sentenceRepository = sentenceRepository;
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
                    throw new RuntimeException("User not found in database for email: " + email);
                }
            } catch (Exception e) {
                System.err.println("Failed to get user ID for email " + email + ": " + e.getMessage());
                throw new RuntimeException("Failed to get user ID for email " + email, e);
            }
        }
        throw new RuntimeException("No authentication found");
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
            System.err.println("Failed to get content approvers from service: " + e.getMessage());
            throw new RuntimeException("Failed to get content approvers", e);
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
                        .fromUserId(fromUserId != null ? fromUserId : getCurrentUserId())
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
        
        // Content creator can modify topics they created or are assigned to
        if ("ROLE_CONTENT_CREATOR".equals(userRole)) {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            // Can modify if they are the creator
            if (currentUserId.equals(topic.getCreatedBy())) {
                return true;
            }
            
            // Can modify if they are assigned via updated_by field
            if (topic.getUpdatedBy() != null && currentUserId.equals(topic.getUpdatedBy())) {
                return true;
            }
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
            // For content approvers, include both parent and child topics when no createdBy specified
            if ("active".equalsIgnoreCase(status)) {
                topicPage = topicRepository.findByParentIsNullAndStatusAndDeletedAtIsNullOrderBySortOrderAsc(status, pageable);
            } else {
                // For pending status, include all topics (both parent and child) so content approvers can see everything
                topicPage = topicRepository.findByStatusAndDeletedAtIsNull(status, pageable);
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
            topic.setUpdatedBy(currentUserId);
            topicRepository.save(topic);
        }
        
        // Create child topic for curriculum change approval
        createChildTopicForCurriculumChange(items);
    }

    @Override
    public TopicDetailResponse getTopicDetail(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        List<SubTopic> subTopics = subTopicRepository.findByTopic_IdAndDeletedAtIsNull(topicId);
        List<SubTopicDetailResponse> subtopicResponses = subTopics.stream().map(sub -> {
            List<Vocab> vocabs = vocabRepository.findBySubTopic_IdAndDeletedAtIsNull(sub.getId());
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
    public TopicDetailResponse getChildTopic(Long parentTopicId) {
        System.out.println("🔍 TopicService.getChildTopic - parentTopicId: " + parentTopicId);
        List<Topic> children = topicRepository.findByParent_IdAndDeletedAtIsNull(parentTopicId);
        if (children.isEmpty()) {
            System.out.println("🔍 TopicService.getChildTopic - no child topic found");
            throw new RuntimeException("Không tìm thấy child topic cho parent topic với id: " + parentTopicId);
        }
        Topic child = children.get(0);
        System.out.println("🔍 TopicService.getChildTopic - found child topic: " + child.getTopicName());
        
        List<SubTopic> subTopics = subTopicRepository.findByTopic_IdAndDeletedAtIsNull(child.getId());
        List<SubTopicDetailResponse> subtopicResponses = subTopics.stream().map(sub -> {
            List<Vocab> vocabs = vocabRepository.findBySubTopic_IdAndDeletedAtIsNull(sub.getId());
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
                .id(child.getId())
                .topicName(child.getTopicName())
                .isFree(child.getIsFree())
                .status(child.getStatus())
                .sortOrder(child.getSortOrder())
                .subtopics(subtopicResponses)
                .createdAt(child.getCreatedAt())
                .createdBy(child.getCreatedBy())
                .updatedAt(child.getUpdatedAt())
                .updatedBy(child.getUpdatedBy())
                .deletedAt(child.getDeletedAt())
                .deletedBy(child.getDeletedBy())
                .build();
    }
    
    @Override
    public TopicDetailResponse createTopic(TopicCreateRequest request) {
        Long currentUserId = getCurrentUserId();

        // Validate duplicate active topic name
        String normalizedName = request.getTopicName() != null ? request.getTopicName().trim() : "";
        if (normalizedName.isEmpty()) {
            throw new RuntimeException("Tên chủ đề không được để trống");
        }
        boolean activeExists = topicRepository.existsByTopicNameIgnoreCaseAndStatusAndDeletedAtIsNull(normalizedName, "active");
        if (activeExists) {
            throw new RuntimeException("Chủ đề với tên này đang hoạt động, không thể tạo trùng");
        }
        
        Topic topic = Topic.builder()
                .topicName(normalizedName)
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .status("pending")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0L)
                .createdAt(Instant.now())
                .createdBy(currentUserId)
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
                            .createdBy(currentUserId)
                            .build();
                    SubTopic savedSub = subTopicRepository.save(subtopic);
                    
                    // Tạo vocab cho subtopic này nếu có
                    if (subReq.getVocabs() != null && !subReq.getVocabs().isEmpty()) {
                        for (VocabCreateRequest vocabReq : subReq.getVocabs()) {
                            if (vocabReq.getVocab() != null && !vocabReq.getVocab().trim().isEmpty()) {
                                Vocab vocab = Vocab.builder()
                                        .vocab(vocabReq.getVocab().trim())
                                        .meaning(vocabReq.getMeaning())
                                        .subTopic(savedSub)
                                        .status("pending")
                                        .createdAt(Instant.now())
                                        .createdBy(currentUserId)
                                        .build();
                                vocabRepository.save(vocab);
                            }
                        }
                    }
                }
            }
        }
        
        // Thông báo cho Content Approver về topic mới cần duyệt
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
        topic.setUpdatedBy(currentUserId);
        
        Topic updatedTopic = topicRepository.save(topic);
        
        // Xử lý subtopics và vocabs
        if (request.getSubtopics() != null && !request.getSubtopics().isEmpty()) {
            // Soft-delete tất cả subtopics cũ của topic này
            List<SubTopic> existingSubtopics = subTopicRepository.findByTopic_IdAndDeletedAtIsNull(topicId);
            for (SubTopic existingSub : existingSubtopics) {
                // Soft-delete tất cả vocabs của subtopic này
                List<Vocab> existingVocabs = vocabRepository.findBySubTopic_IdAndDeletedAtIsNull(existingSub.getId());
                for (Vocab existingVocab : existingVocabs) {
                    existingVocab.setDeletedAt(Instant.now());
                    existingVocab.setDeletedBy(currentUserId);
                    vocabRepository.save(existingVocab);
                }
                // Soft-delete subtopic
                existingSub.setDeletedAt(Instant.now());
                existingSub.setDeletedBy(currentUserId);
                subTopicRepository.save(existingSub);
            }
            
            // Tạo subtopics mới
            for (SubTopicRequest subReq : request.getSubtopics()) {
                if (subReq.getSubTopicName() != null && !subReq.getSubTopicName().trim().isEmpty()) {
                    SubTopic subtopic = SubTopic.builder()
                            .topic(updatedTopic)
                            .subTopicName(subReq.getSubTopicName().trim())
                            .status("pending")
                            .sortOrder(subReq.getSortOrder() != null ? subReq.getSortOrder() : 0L)
                            .createdAt(Instant.now())
                            .createdBy(currentUserId)
                            .build();
                    SubTopic savedSub = subTopicRepository.save(subtopic);
                    
                    // Tạo vocab cho subtopic này nếu có
                    if (subReq.getVocabs() != null && !subReq.getVocabs().isEmpty()) {
                        for (VocabCreateRequest vocabReq : subReq.getVocabs()) {
                            if (vocabReq.getVocab() != null && !vocabReq.getVocab().trim().isEmpty()) {
                                Vocab vocab = Vocab.builder()
                                        .vocab(vocabReq.getVocab().trim())
                                        .meaning(vocabReq.getMeaning())
                                        .subTopic(savedSub)
                                        .status("pending")
                                        .createdAt(Instant.now())
                                        .createdBy(currentUserId)
                                        .build();
                                vocabRepository.save(vocab);
                            }
                        }
                    }
                }
            }
        }
        
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
        topic.setUpdatedBy(currentUserId);
        
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
                .fromUserId(currentUserId)
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
        topic.setDeletedBy(currentUserId);
        
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
        // Calculate subtopic count
        Long subtopicCount;
        if (topic.getParent() != null) {
            // For preview topics (child topics), get subtopic count from parent
            subtopicCount = (long) subTopicRepository.findByTopic_IdAndDeletedAtIsNull(topic.getParent().getId()).size();
        } else {
            // For regular topics, calculate actual subtopic count
            subtopicCount = (long) subTopicRepository.findByTopic_IdAndDeletedAtIsNull(topic.getId()).size();
        }
        
        return TopicDetailResponse.builder()
                .id(topic.getId())
                .topicName(topic.getTopicName())
                .isFree(topic.getIsFree())
                .status(topic.getStatus())
                .sortOrder(topic.getSortOrder())
                .subtopicCount(subtopicCount)
                .parentId(topic.getParent() != null ? topic.getParent().getId() : null)
                .createdAt(topic.getCreatedAt())
                .createdBy(topic.getCreatedBy())
                .updatedAt(topic.getUpdatedAt())
                .updatedBy(topic.getUpdatedBy())
                .deletedAt(topic.getDeletedAt())
                .deletedBy(topic.getDeletedBy())
                .build();
    }

    private SubTopicDetailResponse convertToSubTopicDetailResponse(SubTopic subtopic) {
        List<Vocab> vocabs = vocabRepository.findBySubTopic_IdAndDeletedAtIsNull(subtopic.getId());
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
        String topicName = null;
        Long topicId = null;
        String subTopicName = null;
        
        // Get topic info from SubTopic
        if (vocab.getSubTopic() != null) {
            subTopicName = vocab.getSubTopic().getSubTopicName();
            if (vocab.getSubTopic().getTopic() != null) {
                topicName = vocab.getSubTopic().getTopic().getTopicName();
                topicId = vocab.getSubTopic().getTopic().getId();
            }
        }
        
        return VocabDetailResponse.builder()
            .id(vocab.getId())
            .vocab(vocab.getVocab())
            .topicName(topicName)
            .topicId(topicId)
            .subTopicName(subTopicName)
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
    public boolean checkDuplicateTopicName(String topicName, String excludeStatus) {
        if (topicName == null || topicName.trim().isEmpty()) {
            return false; // Empty name is not considered duplicate
        }
        
        String normalizedName = topicName.trim();
        
        // Check for active topics
        boolean activeExists = topicRepository.existsByTopicNameIgnoreCaseAndStatusAndDeletedAtIsNull(normalizedName, "active");
        if (activeExists) {
            return true;
        }
        
        // Check for pending topics
        boolean pendingExists = topicRepository.existsByTopicNameIgnoreCaseAndStatusAndDeletedAtIsNull(normalizedName, "pending");
        if (pendingExists) {
            return true;
        }
        
        // Check for draft topics (if not excluded)
        if (excludeStatus == null || !excludeStatus.equals("draft")) {
            boolean draftExists = topicRepository.existsByTopicNameIgnoreCaseAndStatusAndDeletedAtIsNull(normalizedName, "draft");
            if (draftExists) {
                return true;
            }
        }
        
        return false;
    }

    private void cloneSubtopicsAndVocabs(Topic parent, Topic child) {
        // Get existing subtopics from parent
        List<SubTopic> parentSubtopics = subTopicRepository.findByTopic_IdAndDeletedAtIsNull(parent.getId());
        
        // Delete existing subtopics in child (if any)
        List<SubTopic> existingChildSubtopics = subTopicRepository.findByTopic_IdAndDeletedAtIsNull(child.getId());
        for (SubTopic existingSub : existingChildSubtopics) {
            // Delete associated vocabs first
            List<Vocab> existingVocabs = vocabRepository.findBySubTopic_IdAndDeletedAtIsNull(existingSub.getId());
            for (Vocab existingVocab : existingVocabs) {
                existingVocab.setDeletedAt(Instant.now());
                existingVocab.setDeletedBy(getCurrentUserId());
                vocabRepository.save(existingVocab);
            }
            // Delete subtopic
            existingSub.setDeletedAt(Instant.now());
            existingSub.setDeletedBy(getCurrentUserId());
            subTopicRepository.save(existingSub);
        }
        
        // Clone subtopics and vocabs from parent to child
        for (SubTopic parentSub : parentSubtopics) {
            // Create new subtopic for child
            SubTopic childSub = SubTopic.builder()
                    .subTopicName(parentSub.getSubTopicName())
                    .sortOrder(parentSub.getSortOrder())
                    .status(parentSub.getStatus()) // Clone status from parent
                    .topic(child)
                    .createdAt(Instant.now())
                    .createdBy(getCurrentUserId())
                    .build();
            SubTopic savedChildSub = subTopicRepository.save(childSub);
            
            // Clone vocabs for this subtopic
            List<Vocab> parentVocabs = vocabRepository.findBySubTopic_IdAndDeletedAtIsNull(parentSub.getId());
            for (Vocab parentVocab : parentVocabs) {
                Vocab childVocab = Vocab.builder()
                        .vocab(parentVocab.getVocab())
                        .meaning(parentVocab.getMeaning())
                        .subTopic(savedChildSub)
                        .status(parentVocab.getStatus())
                        .createdAt(Instant.now())
                        .createdBy(getCurrentUserId())
                        .build();
                vocabRepository.save(childVocab);
            }
        }
        
        // Clone sentences from parent to child
        List<Sentence> parentSentences = sentenceRepository.findBySentenceTopicId(parent.getId());
        for (Sentence parentSentence : parentSentences) {
            Sentence childSentence = Sentence.builder()
                    .sentenceMeaning(parentSentence.getSentenceMeaning())
                    .sentenceDescription(parentSentence.getSentenceDescription())
                    .sentenceVideo(parentSentence.getSentenceVideo())
                    .sentenceTopic(child)
                    .createdAt(Instant.now())
                    .createdBy(getCurrentUserId())
                    .build();
            sentenceRepository.save(childSentence);
        }
    }

    @Override
    public TopicDetailResponse requestUpdate(Long parentTopicId, Long assigneeUserId, String message) {
        Topic parent = getParentTopicOrThrow(parentTopicId);
        // Only approver/GM should call this (checked at controller). Set parent to request_update
        parent.setStatus("request_update");
        parent.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        parent.setUpdatedBy(currentUserId);
        topicRepository.save(parent);
        // Ensure a child draft exists
        Topic child = getOrCreateChildDraft(parent);
        // Notify creator or assignee
        Long toUserId = (assigneeUserId != null) ? assigneeUserId : parent.getCreatedBy();
        String content = String.format("Chủ đề \"%s\" cần chỉnh sửa. Chỉnh sửa lại [topic:%d]%s",
                parent.getTopicName(), parent.getId(), (message != null && !message.isBlank() ? ("\nGhi chú: " + message) : ""));
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
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
        child.setUpdatedBy(currentUserId);
        Topic saved = topicRepository.save(child);
        
        // Clone subtopics and vocabs from parent to child
        cloneSubtopicsAndVocabs(parent, child);
        
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
        child.setUpdatedBy(currentUserId);
        Topic saved = topicRepository.save(child);
        // Notify creator about submission
        String content = String.format("Đã gửi cập nhật cho chủ đề \"%s\". Xem chi tiết [topic:%d]", parent.getTopicName(), parent.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
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
        
        // Merge basic fields back to parent
        parent.setTopicName(child.getTopicName());
        parent.setIsFree(child.getIsFree());
        parent.setSortOrder(child.getSortOrder());
        parent.setStatus("active");
        parent.setUpdatedAt(Instant.now());
        Long currentUserId = getCurrentUserId();
        parent.setUpdatedBy(currentUserId);
        
        // Save parent with updated basic info
        Topic updatedParent = topicRepository.save(parent);
        
        // Soft-delete child
        child.setDeletedAt(Instant.now());
        child.setDeletedBy(currentUserId);
        topicRepository.save(child);
        
        // Notify creator approved
        String content = String.format("Cập nhật chủ đề \"%s\" đã được phê duyệt. Xem chi tiết [topic:%d]", updatedParent.getTopicName(), updatedParent.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
                .toUserId(updatedParent.getCreatedBy())
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
        child.setUpdatedBy(currentUserId);
        topicRepository.save(child);
        // Keep parent in request_update until approved
        parent.setStatus("request_update");
        parent.setUpdatedAt(Instant.now());
        parent.setUpdatedBy(currentUserId);
        Topic savedParent = topicRepository.save(parent);
        // Notify creator rejected
        String content = String.format("Cập nhật chủ đề \"%s\" bị từ chối. Chỉnh sửa lại [topic:%d]", parent.getTopicName(), parent.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
                .toUserId(parent.getCreatedBy())
                .build());
        return convertToTopicDetailResponse(savedParent);
    }

    @Override
    public TopicDetailResponse createDraftTopic(TopicCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        
        Topic topic = Topic.builder()
                .topicName(request.getTopicName())
                .isFree(request.getIsFree() != null ? request.getIsFree() : false)
                .status("draft")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0L)
                .createdAt(Instant.now())
                .createdBy(currentUserId)
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
                            .createdBy(currentUserId)
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
                .createdBy(getCurrentUserId())
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
        childTopic.setUpdatedBy(currentUserId);
        
        Topic savedChildTopic = topicRepository.save(childTopic);
        
        // Notify creator that curriculum change was approved
        String content = String.format("Yêu cầu thay đổi lộ trình học đã được phê duyệt.");
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
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
        childTopic.setDeletedBy(currentUserId);
        
        Topic savedChildTopic = topicRepository.save(childTopic);
        
        // Notify creator that curriculum change was rejected
        String content = String.format("Yêu cầu thay đổi lộ trình học bị từ chối. Lý do: %s", reason != null ? reason : "Không có lý do");
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
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

    // ==================== CURRICULUM PREVIEW WORKFLOW (Option 1 - Parent-Child) ====================

    @Override
    @Transactional
    public void createCurriculumPreview(List<Map<String, Object>> items) {
        // 1. Clear any existing previews
        List<Topic> existingPreviews = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNull("pending");
        if (!existingPreviews.isEmpty()) {
            topicRepository.deleteAll(existingPreviews);
        }
        
        // 2. Get current live topics (parent = null, status = active)
        List<Topic> liveTopics = topicRepository.findByParentIsNullAndStatusAndDeletedAtIsNull("active");
        
        // 3. Create preview topics (child topics with status = pending)
        for (Map<String, Object> item : items) {
            Long topicId = Long.valueOf(item.get("id").toString());
            Long newSortOrder = Long.valueOf(item.get("sortOrder").toString());
            
            // Find the corresponding live topic
            Topic liveTopic = liveTopics.stream()
                .filter(topic -> topic.getId().equals(topicId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
            
            // Create preview topic
            Topic previewTopic = Topic.builder()
                .topicName(liveTopic.getTopicName())
                .isFree(liveTopic.getIsFree())
                .status("pending")
                .sortOrder(newSortOrder)
                .parent(liveTopic)  // Set parent = live topic
                .createdAt(Instant.now())
                .createdBy(getCurrentUserId())
                .build();
            
            // Copy subtopic count from parent topic
            // Note: We'll calculate this in the response, not store in preview
            
            topicRepository.save(previewTopic);
        }
        
        // 4. Notify content approvers
        List<Long> approverIds = getContentApproverIds();
        String content = "Có yêu cầu thay đổi thứ tự lộ trình học mới cần duyệt.";
        
        for (Long approverId : approverIds) {
            notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(getCurrentUserId())
                .toUserId(approverId)
                .build());
        }
    }

    @Override
    public List<TopicDetailResponse> getCurriculumPreviews() {
        // Get all preview topics (child topics with status = pending)
        List<Topic> previewTopics = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNullOrderBySortOrderAsc("pending");
        
        return previewTopics.stream()
            .map(this::convertToTopicDetailResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveCurriculumPreview() {
        // 1. Get all preview topics
        List<Topic> previewTopics = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNull("pending");
        
        if (previewTopics.isEmpty()) {
            throw new RuntimeException("Không có preview nào để duyệt");
        }
        
        // 2. Update live topics with new sort order
        for (Topic preview : previewTopics) {
            Topic liveTopic = preview.getParent();
            if (liveTopic != null) {
                liveTopic.setSortOrder(preview.getSortOrder());
                liveTopic.setUpdatedAt(Instant.now());
                liveTopic.setUpdatedBy(getCurrentUserId());
                topicRepository.save(liveTopic);
            }
        }
        
        // 3. Delete all preview topics
        List<Topic> previewsToDelete = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNull("pending");
        topicRepository.deleteAll(previewsToDelete);
        
        // 4. Notify creator that preview was approved
        Long creatorId = previewTopics.get(0).getCreatedBy();
        String content = "Yêu cầu thay đổi thứ tự lộ trình học đã được phê duyệt.";
        notificationService.createNotification(NotificationCreateRequest.builder()
            .content(content)
            .fromUserId(getCurrentUserId())
            .toUserId(creatorId)
            .build());
    }

    @Override
    @Transactional
    public void rejectCurriculumPreview(String reason) {
        // 1. Get creator ID before deleting previews
        List<Topic> previewTopics = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNull("pending");
        
        if (previewTopics.isEmpty()) {
            throw new RuntimeException("Không có preview nào để từ chối");
        }
        
        Long creatorId = previewTopics.get(0).getCreatedBy();
        
        // 2. Delete all preview topics
        List<Topic> previewsToDelete = topicRepository.findByParentIsNotNullAndStatusAndDeletedAtIsNull("pending");
        topicRepository.deleteAll(previewsToDelete);
        
        // 3. Notify creator that preview was rejected
        String content = String.format("Yêu cầu thay đổi thứ tự lộ trình học bị từ chối. Lý do: %s", 
            reason != null ? reason : "Không có lý do");
        notificationService.createNotification(NotificationCreateRequest.builder()
            .content(content)
            .fromUserId(getCurrentUserId())
            .toUserId(creatorId)
            .build());
    }

    @Override
    public TopicDetailResponse assignTopicPermission(Long topicId, Long assigneeUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        // Kiểm tra quyền ủy quyền (chỉ người tạo mới có thể ủy quyền)
        String userRole = getCurrentUserRole();
        Long currentUserId = getCurrentUserId();
        
        if (!"ROLE_GENERAL_MANAGER".equals(userRole) && 
            !"ROLE_CONTENT_APPROVER".equals(userRole) && 
            !(currentUserId != null && currentUserId.equals(topic.getCreatedBy()))) {
            throw new RuntimeException("Bạn không có quyền ủy quyền chủ đề này");
        }
        
        // Kiểm tra người được ủy quyền có tồn tại và là content creator không
        // TODO: Có thể thêm validation để kiểm tra user có role content-creator
        
        // Ủy quyền bằng cách set updated_by
        topic.setUpdatedBy(assigneeUserId);
        topic.setUpdatedAt(Instant.now());
        
        Topic savedTopic = topicRepository.save(topic);
        
        // Thông báo cho người được ủy quyền
        String content = String.format("Bạn đã được ủy quyền chỉnh sửa chủ đề \"%s\". Xem chi tiết [topic:%d]", 
                topic.getTopicName(), topic.getId());
        notificationService.createNotification(NotificationCreateRequest.builder()
                .content(content)
                .fromUserId(currentUserId)
                .toUserId(assigneeUserId)
                .build());
        
        return convertToTopicDetailResponse(savedTopic);
    }
    
    @Override
    public TopicDetailResponse revokeTopicPermission(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        // Kiểm tra quyền hủy ủy quyền (chỉ người tạo mới có thể hủy)
        String userRole = getCurrentUserRole();
        Long currentUserId = getCurrentUserId();
        
        if (!"ROLE_GENERAL_MANAGER".equals(userRole) && 
            !"ROLE_CONTENT_APPROVER".equals(userRole) && 
            !(currentUserId != null && currentUserId.equals(topic.getCreatedBy()))) {
            throw new RuntimeException("Bạn không có quyền hủy ủy quyền chủ đề này");
        }
        
        // Hủy ủy quyền bằng cách set updated_by về null
        Long previousAssignee = topic.getUpdatedBy();
        topic.setUpdatedBy(null);
        topic.setUpdatedAt(Instant.now());
        
        Topic savedTopic = topicRepository.save(topic);
        
        // Thông báo cho người bị hủy ủy quyền
        if (previousAssignee != null) {
            String content = String.format("Quyền chỉnh sửa chủ đề \"%s\" đã bị thu hồi.", topic.getTopicName());
            notificationService.createNotification(NotificationCreateRequest.builder()
                    .content(content)
                    .fromUserId(currentUserId)
                    .toUserId(previousAssignee)
                    .build());
        }
        
        return convertToTopicDetailResponse(savedTopic);
    }

    @Override
    public TopicDetailResponse resolveTopic(Long topicId) {
        // Try direct
        Topic topic = topicRepository.findById(topicId)
                .orElse(null);
        if (topic == null) {
            // If not found directly, maybe topicId is a child id that no longer exists
            throw new RuntimeException("Topic not found with id: " + topicId);
        }
        // If this is a child, we may want to return its detail directly
        // For detail purposes, just reuse getTopicDetail to assemble sub-entities
        return getTopicDetail(topic.getId());
    }
} 