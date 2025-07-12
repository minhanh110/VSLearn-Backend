package com.vslearn.controller;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(topicService.getTopicList(PageRequest.of(page, size), search));
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<TopicDetailResponse> getTopicDetail(@PathVariable Long topicId) {
        try {
            return ResponseEntity.ok(topicService.getTopicDetail(topicId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTopic(@RequestBody TopicCreateRequest request) {
        try {
            TopicDetailResponse response = topicService.createTopic(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo chủ đề thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{topicId}")
    public ResponseEntity<Map<String, Object>> updateTopic(@PathVariable Long topicId, @RequestBody TopicUpdateRequest request) {
        try {
            TopicDetailResponse response = topicService.updateTopic(topicId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật chủ đề thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

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

    @PutMapping("/{topicId}/status")
    public ResponseEntity<Map<String, Object>> updateTopicStatus(@PathVariable Long topicId, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Trạng thái không được để trống"));
        }
        
        try {
            TopicDetailResponse response = topicService.updateTopicStatus(topicId, status);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

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
} 