package com.vslearn.controller;

import com.vslearn.dto.request.VocabCreateRequest;
import com.vslearn.dto.request.VocabUpdateRequest;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.dto.response.VocabListResponse;
import com.vslearn.entities.Vocab;
import com.vslearn.entities.SubTopic;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/vocab")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class VocabController {
    private final VocabService vocabService;
    private final VocabRepository vocabRepository;
    private final SubTopicRepository subTopicRepository;
    private final com.google.cloud.storage.Storage storage;
    private final String bucketName;

    @Autowired
    public VocabController(VocabService vocabService, VocabRepository vocabRepository, SubTopicRepository subTopicRepository,
                         com.google.cloud.storage.Storage storage, @Value("${gcp.storage.bucket.name}") String bucketName) {
        this.vocabService = vocabService;
        this.vocabRepository = vocabRepository;
        this.subTopicRepository = subTopicRepository;
        this.storage = storage;
        this.bucketName = bucketName;
    }

    // Lấy danh sách từ vựng với phân trang và filter
    @GetMapping("/list")
    public ResponseEntity<VocabListResponse> getVocabList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String letter,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        VocabListResponse response = vocabService.getVocabList(pageable, search, topic, region, letter, status);
        return ResponseEntity.ok(response);
    }

    // Lấy chi tiết từ vựng theo ID
    @GetMapping("/{vocabId}")
    public ResponseEntity<VocabDetailResponse> getVocabDetail(@PathVariable Long vocabId) {
        Optional<Vocab> vocab = vocabRepository.findById(vocabId);
        if (vocab.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        VocabDetailResponse response = vocabService.getVocabDetail(vocabId);
        return ResponseEntity.ok(response);
    }

    // Tạo từ vựng mới
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createVocab(@RequestBody VocabCreateRequest request) {
        try {
            VocabDetailResponse response = vocabService.createVocab(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo từ vựng thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Cập nhật từ vựng
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PutMapping("/{vocabId}")
    public ResponseEntity<Map<String, Object>> updateVocab(
            @PathVariable Long vocabId,
            @RequestBody VocabUpdateRequest request) {
        try {
            VocabDetailResponse response = vocabService.updateVocab(vocabId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật từ vựng thành công",
                "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Vô hiệu hóa từ vựng
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @DeleteMapping("/{vocabId}")
    public ResponseEntity<Map<String, Object>> disableVocab(@PathVariable Long vocabId) {
        try {
            vocabService.disableVocab(vocabId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vô hiệu hóa từ vựng thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Lấy danh sách từ vựng bị từ chối
    @GetMapping("/rejected")
    public ResponseEntity<VocabListResponse> getRejectedVocabList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        VocabListResponse response = vocabService.getRejectedVocabList(pageable);
        return ResponseEntity.ok(response);
    }

    // Lấy danh sách chủ đề
    @GetMapping("/topics")
    public ResponseEntity<List<Map<String, Object>>> getTopics() {
        List<Map<String, Object>> topics = vocabService.getTopics();
        return ResponseEntity.ok(topics);
    }

    // Lấy danh sách khu vực
    @GetMapping("/regions")
    public ResponseEntity<List<Map<String, Object>>> getRegions() {
        List<Map<String, Object>> regions = vocabService.getRegions();
        return ResponseEntity.ok(regions);
    }

    // Cập nhật trạng thái từ vựng (cho Content Approver)
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_APPROVER', 'ROLE_GENERAL_MANAGER')")
    @PutMapping("/{vocabId}/status")
    public ResponseEntity<Map<String, Object>> updateVocabStatus(@PathVariable Long vocabId, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Trạng thái không được để trống"));
        }
        
        try {
            VocabDetailResponse response = vocabService.updateVocabStatus(vocabId, status);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công", "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Serve video files from Google Cloud Storage (same as flashcard)
    @GetMapping("/video/{videoPath}")
    public ResponseEntity<String> getVideo(@PathVariable String videoPath) {
        try {
            // Decode the URL-encoded path
            String decodedPath = java.net.URLDecoder.decode(videoPath, "UTF-8");
            
            // Generate signed URL like flashcard does
            com.google.cloud.storage.BlobId blobId = com.google.cloud.storage.BlobId.of(bucketName, decodedPath);
            com.google.cloud.storage.BlobInfo blobInfo = com.google.cloud.storage.BlobInfo.newBuilder(blobId).build();
            
            java.net.URL signedUrl = storage.signUrl(blobInfo, 2, java.util.concurrent.TimeUnit.HOURS, 
                com.google.cloud.storage.Storage.SignUrlOption.withV4Signature());
            
            System.out.println("🔍 Signed Video URL: " + signedUrl);
            
            // Redirect to signed URL
            return ResponseEntity.status(302)
                .header("Location", signedUrl.toString())
                .build();
                
        } catch (Exception e) {
            System.out.println("❌ Error serving video: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
} 