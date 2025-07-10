package com.vslearn.controller;

import com.vslearn.dto.request.TopicCreateRequest;
import com.vslearn.dto.request.TopicUpdateRequest;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.dto.response.TopicListResponse;
import com.vslearn.entities.Topic;
import com.vslearn.repository.TopicRepository;
import com.vslearn.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/topics")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TopicController {
    private final TopicService topicService;
    private final TopicRepository topicRepository;

    @Autowired
    public TopicController(TopicService topicService, TopicRepository topicRepository) {
        this.topicService = topicService;
        this.topicRepository = topicRepository;
    }

    // Lấy danh sách chủ đề với phân trang
    @GetMapping("/list")
    public ResponseEntity<TopicListResponse> getTopicList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        TopicListResponse response = topicService.getTopicList(pageable, search);
        return ResponseEntity.ok(response);
    }

    // Lấy chi tiết chủ đề theo ID
    @GetMapping("/{topicId}")
    public ResponseEntity<TopicDetailResponse> getTopicDetail(@PathVariable Long topicId) {
        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TopicDetailResponse response = topicService.getTopicDetail(topicId);
        return ResponseEntity.ok(response);
    }

    // Tạo chủ đề mới
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTopic(@RequestBody TopicCreateRequest request) {
        try {
            TopicDetailResponse response = topicService.createTopic(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo chủ đề thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Cập nhật chủ đề
    @PutMapping("/{topicId}")
    public ResponseEntity<Map<String, Object>> updateTopic(
            @PathVariable Long topicId,
            @RequestBody TopicUpdateRequest request) {
        try {
            TopicDetailResponse response = topicService.updateTopic(topicId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật chủ đề thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Vô hiệu hóa chủ đề
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Map<String, Object>> disableTopic(@PathVariable Long topicId) {
        try {
            topicService.disableTopic(topicId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vô hiệu hóa chủ đề thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Lấy tất cả chủ đề (không phân trang)
    @GetMapping("/all")
    public ResponseEntity<List<TopicDetailResponse>> getAllTopics() {
        List<TopicDetailResponse> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }
} 