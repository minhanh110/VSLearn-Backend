package com.vslearn.controller;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.request.RequestUpdateRequest;
import com.vslearn.dto.response.ReviewHistoryEntry;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/topics")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TopicController {
    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/list")
    public ResponseEntity<TopicListResponse> getTopicList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long createdBy // thêm dòng này
    ) {
        return ResponseEntity.ok(topicService.getTopicList(PageRequest.of(page, size), search, status, createdBy));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicDetailResponse> getTopicDetail(@PathVariable Long topicId) {
        try {
            return ResponseEntity.ok(topicService.getTopicDetail(topicId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTopic(@RequestBody TopicCreateRequest request) {
        try {
            TopicDetailResponse response = topicService.createTopic(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo chủ đề thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PutMapping("/{topicId}")
    public ResponseEntity<Map<String, Object>> updateTopic(@PathVariable Long topicId, @RequestBody TopicUpdateRequest request) {
        try {
            TopicDetailResponse response = topicService.updateTopic(topicId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật chủ đề thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Map<String, Object>> disableTopic(@PathVariable Long topicId) {
        try {
            topicService.disableTopic(topicId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa mềm chủ đề thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<TopicDetailResponse>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PutMapping("/{topicId}/status")
    public ResponseEntity<Map<String, Object>> updateTopicStatus(@PathVariable Long topicId, @RequestBody Map<String, String> request) {
        System.out.println("🔍 Controller updateTopicStatus - topicId: " + topicId + ", request: " + request);
        
        String status = request.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Trạng thái không được để trống"));
        }
        
        System.out.println("🔍 Controller - status to update: " + status);
        
        try {
            TopicDetailResponse response = topicService.updateTopicStatus(topicId, status);
            System.out.println("🔍 Controller - response status: " + response.getStatus());
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công", "data", response));
        } catch (Exception e) {
            System.out.println("🔍 Controller - error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PutMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorderTopics(@RequestBody Map<String, List<Map<String, Object>>> request) {
        List<Map<String, Object>> items = request.get("items");
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Danh sách trống"));
        }
        try {
            topicService.reorderTopics(items);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thứ tự thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
    @GetMapping("/status-options")
    public ResponseEntity<List<Map<String, Object>>> getStatusOptions() {
        List<Map<String, Object>> statusOptions = List.of(
            Map.of("value", "active", "label", "Hoạt động", "description", "Chủ đề đang hoạt động và có thể sử dụng"),
            Map.of("value", "pending", "label", "Đang kiểm duyệt", "description", "Chủ đề đang chờ phê duyệt"),
            Map.of("value", "rejected", "label", "Bị từ chối", "description", "Chủ đề đã bị từ chối"),
            Map.of("value", "inactive", "label", "Không hoạt động", "description", "Chủ đề đã bị vô hiệu hóa")
        );
        return ResponseEntity.ok(statusOptions);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/request-update")
    public ResponseEntity<Map<String, Object>> requestUpdate(@PathVariable Long topicId, @RequestBody(required = false) RequestUpdateRequest body) {
        try {
            TopicDetailResponse response = topicService.requestUpdate(topicId,
                    body != null ? body.getAssigneeUserId() : null,
                    body != null ? body.getMessage() : null);
            return ResponseEntity.ok(Map.of("success", true, "message", "Yêu cầu chỉnh sửa đã được tạo", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/save-draft")
    public ResponseEntity<Map<String, Object>> saveDraft(@PathVariable Long topicId, @RequestBody TopicUpdateRequest request) {
        try {
            TopicDetailResponse response = topicService.saveDraft(topicId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu nháp bản cập nhật thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/submit-update")
    public ResponseEntity<Map<String, Object>> submitUpdate(@PathVariable Long topicId) {
        try {
            TopicDetailResponse response = topicService.submitUpdate(topicId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Gửi duyệt cập nhật thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/approve-update")
    public ResponseEntity<Map<String, Object>> approveUpdate(@PathVariable Long topicId) {
        try {
            TopicDetailResponse response = topicService.approveUpdate(topicId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Phê duyệt cập nhật thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/reject-update")
    public ResponseEntity<Map<String, Object>> rejectUpdate(@PathVariable Long topicId) {
        try {
            TopicDetailResponse response = topicService.rejectUpdate(topicId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Từ chối cập nhật, chuyển về nháp", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/assign-permission")
    public ResponseEntity<Map<String, Object>> assignTopicPermission(@PathVariable Long topicId, @RequestBody Map<String, Object> request) {
        try {
            Long assigneeUserId = Long.valueOf(request.get("assigneeUserId").toString());
            TopicDetailResponse response = topicService.assignTopicPermission(topicId, assigneeUserId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Ủy quyền thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{topicId}/revoke-permission")
    public ResponseEntity<Map<String, Object>> revokeTopicPermission(@PathVariable Long topicId) {
        try {
            TopicDetailResponse response = topicService.revokeTopicPermission(topicId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Thu hồi ủy quyền thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/create-draft")
    public ResponseEntity<Map<String, Object>> createDraftTopic(@RequestBody TopicCreateRequest request) {
        try {
            TopicDetailResponse response = topicService.createDraftTopic(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo nháp chủ đề thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // New: Curriculum change workflow endpoints
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @GetMapping("/curriculum-requests")
    public ResponseEntity<Map<String, Object>> getCurriculumRequests() {
        try {
            List<TopicDetailResponse> requests = topicService.getCurriculumRequests();
            return ResponseEntity.ok(Map.of("success", true, "data", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/curriculum-requests/{childTopicId}/approve")
    public ResponseEntity<Map<String, Object>> approveCurriculumRequest(@PathVariable Long childTopicId) {
        try {
            TopicDetailResponse response = topicService.approveCurriculumRequest(childTopicId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Phê duyệt thay đổi lộ trình học thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/curriculum-requests/{childTopicId}/reject")
    public ResponseEntity<Map<String, Object>> rejectCurriculumRequest(@PathVariable Long childTopicId, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            TopicDetailResponse response = topicService.rejectCurriculumRequest(childTopicId, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Từ chối thay đổi lộ trình học thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // New: Review history endpoint
    @GetMapping("/{topicId}/review-history")
    public ResponseEntity<List<ReviewHistoryEntry>> getTopicReviewHistory(@PathVariable Long topicId) {
        try {
            List<ReviewHistoryEntry> history = topicService.getTopicReviewHistory(topicId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== CURRICULUM PREVIEW WORKFLOW ENDPOINTS ====================

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/curriculum-preview")
    public ResponseEntity<Map<String, Object>> createCurriculumPreview(@RequestBody List<Map<String, Object>> items) {
        try {
            topicService.createCurriculumPreview(items);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo preview thay đổi lộ trình học thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @GetMapping("/curriculum-previews")
    public ResponseEntity<Map<String, Object>> getCurriculumPreviews() {
        try {
            List<TopicDetailResponse> previews = topicService.getCurriculumPreviews();
            return ResponseEntity.ok(Map.of("success", true, "data", previews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/curriculum-preview/approve")
    public ResponseEntity<Map<String, Object>> approveCurriculumPreview() {
        try {
            topicService.approveCurriculumPreview();
            return ResponseEntity.ok(Map.of("success", true, "message", "Phê duyệt thay đổi lộ trình học thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/curriculum-preview/reject")
    public ResponseEntity<Map<String, Object>> rejectCurriculumPreview(@RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            topicService.rejectCurriculumPreview(reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "Từ chối thay đổi lộ trình học thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
} 