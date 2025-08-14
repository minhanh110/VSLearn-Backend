package com.vslearn.controller;

import com.vslearn.dto.request.VocabCreateRequest;
import com.vslearn.dto.request.VocabUpdateRequest;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.dto.response.VocabListResponse;
import com.vslearn.dto.response.VideoUploadResponse;
import com.vslearn.dto.VocabularyDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long createdBy
    ) {
        Pageable pageable = PageRequest.of(page, size);
        VocabListResponse response = vocabService.getVocabList(pageable, search, topic, region, letter, status, createdBy);
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

    // Yêu cầu xóa từ vựng (cho content creator)
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    @PostMapping("/{vocabId}/request-delete")
    public ResponseEntity<Map<String, Object>> requestDeleteVocab(@PathVariable Long vocabId, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            vocabService.requestDeleteVocab(vocabId, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã gửi yêu cầu xóa từ vựng thành công"
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

    // Debug endpoint to list files in bucket
    @GetMapping("/debug/list-files")
    public ResponseEntity<Map<String, Object>> listFilesInBucket() {
        try {
            System.out.println("🔍 Listing files in bucket: " + bucketName);
            
            List<String> fileNames = new ArrayList<>();
            for (com.google.cloud.storage.Blob blob : storage.list(bucketName).iterateAll()) {
                fileNames.add(blob.getName());
                System.out.println("📁 Found file: " + blob.getName());
            }
            
            return ResponseEntity.ok(Map.of(
                "bucket", bucketName,
                "fileCount", fileNames.size(),
                "files", fileNames
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error listing files: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Serve video files from Google Cloud Storage (same as flashcard)
    @GetMapping("/video/{topic}/{subtopic}/{filename}")
    public ResponseEntity<String> getVideo(
            @PathVariable String topic,
            @PathVariable String subtopic,
            @PathVariable String filename) {
        try {
            // Construct the full path
            String fullPath = topic + "/" + subtopic + "/" + filename;
            System.out.println("🔍 Topic: " + topic);
            System.out.println("🔍 Subtopic: " + subtopic);
            System.out.println("🔍 Filename: " + filename);
            System.out.println("🔍 Full path: " + fullPath);
            System.out.println("🔍 Bucket name: " + bucketName);
            
            // Generate signed URL like flashcard does
            com.google.cloud.storage.BlobId blobId = com.google.cloud.storage.BlobId.of(bucketName, fullPath);
            com.google.cloud.storage.BlobInfo blobInfo = com.google.cloud.storage.BlobInfo.newBuilder(blobId).build();
            
            System.out.println("🔍 BlobId: " + blobId);
            System.out.println("🔍 BlobInfo: " + blobInfo);
            
            java.net.URL signedUrl = storage.signUrl(blobInfo, 2, java.util.concurrent.TimeUnit.HOURS, 
                com.google.cloud.storage.Storage.SignUrlOption.withV4Signature());
            
            System.out.println("🔍 Signed Video URL: " + signedUrl);
            
            // Redirect to signed URL
            return ResponseEntity.status(302)
                .header("Location", signedUrl.toString())
                .build();
                
        } catch (Exception e) {
            System.out.println("❌ Error serving video: " + e.getMessage());
            System.out.println("❌ Exception type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload-video")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // Validation
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "File không được để trống"
                ));
            }
            
            // File size validation (50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "File quá lớn. Tối đa 50MB"
                ));
            }
            
            // File type validation
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Chỉ chấp nhận file video"
                ));
            }
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            VideoUploadResponse response = vocabService.uploadVideoToGCS(file, fileName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Upload video thành công",
                "data", response
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/video/{fileName}")
    @PreAuthorize("hasAnyAuthority('ROLE_CONTENT_CREATOR', 'ROLE_GENERAL_MANAGER')")
    public ResponseEntity<?> deleteVideo(@PathVariable String fileName) {
        try {
            vocabService.deleteVideoFromGCS(fileName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa video thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // ========== CAMERA SIGN ENDPOINTS ==========
    
    // Lấy tất cả vocabulary cho camera sign (không phân trang)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVocabulary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Vocab> vocabPage = vocabRepository.findByStatus("active", pageable);
            System.out.println("DEBUG - Query executed with status: active");
            System.out.println("DEBUG - Total elements found: " + vocabPage.getTotalElements());
            if (vocabPage.getContent().isEmpty()) {
                System.out.println("DEBUG - No content found!");
            } else {
                System.out.println("DEBUG - First vocab: " + vocabPage.getContent().get(0).getVocab());
                System.out.println("DEBUG - First vocab status: " + vocabPage.getContent().get(0).getStatus());
            }
            
            List<VocabularyDTO> vocabularyList = vocabPage.getContent().stream()
                .map(vocab -> {
                    VocabularyDTO dto = new VocabularyDTO();
                    dto.setId(vocab.getId());
                    dto.setVocab(vocab.getVocab());
                    dto.setMeaning(vocab.getMeaning());
                    
                    // Lấy thông tin từ SubTopic
                    if (vocab.getSubTopic() != null) {
                        dto.setSubTopicName(vocab.getSubTopic().getSubTopicName());
                        if (vocab.getSubTopic().getTopic() != null) {
                            dto.setTopicName(vocab.getSubTopic().getTopic().getTopicName());
                        }
                    }
                    
                    // Lấy thông tin từ VocabArea (video và description)
                    if (vocab.getVocabAreas() != null && !vocab.getVocabAreas().isEmpty()) {
                        var vocabArea = vocab.getVocabAreas().get(0); // Lấy area đầu tiên
                        dto.setVideoUrl(vocabArea.getVocabAreaVideo());
                        dto.setDescription(vocabArea.getVocabAreaDescription());
                        dto.setAreaName(vocabArea.getArea().getAreaName());
                        dto.setCategory(vocabArea.getArea().getAreaName()); // Sử dụng area name làm category
                        
                        // Tính difficulty dựa trên sortOrder
                        if (vocab.getSubTopic() != null && vocab.getSubTopic().getSortOrder() != null) {
                            long sortOrder = vocab.getSubTopic().getSortOrder();
                            if (sortOrder <= 3) dto.setDifficulty("easy");
                            else if (sortOrder <= 6) dto.setDifficulty("medium");
                            else dto.setDifficulty("hard");
                        } else {
                            dto.setDifficulty("medium");
                        }
                    }
                    
                    return dto;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "content", vocabularyList,
                "totalElements", vocabPage.getTotalElements(),
                "totalPages", vocabPage.getTotalPages(),
                "currentPage", page
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to fetch vocabulary: " + e.getMessage()
            ));
        }
    }

    // Lấy vocabulary theo category (area name)
    @GetMapping("/category/{category}")
    public ResponseEntity<List<VocabularyDTO>> getVocabularyByCategory(@PathVariable String category) {
        try {
            // Tìm vocab theo area name
            List<Vocab> vocabList = vocabRepository.findByVocabAreas_Area_AreaNameAndStatus(category, "active");
            
            List<VocabularyDTO> vocabularyList = vocabList.stream()
                .map(vocab -> {
                    VocabularyDTO dto = new VocabularyDTO();
                    dto.setId(vocab.getId());
                    dto.setVocab(vocab.getVocab());
                    dto.setMeaning(vocab.getMeaning());
                    
                    if (vocab.getSubTopic() != null) {
                        dto.setSubTopicName(vocab.getSubTopic().getSubTopicName());
                        if (vocab.getSubTopic().getTopic() != null) {
                            dto.setTopicName(vocab.getSubTopic().getTopic().getTopicName());
                        }
                    }
                    
                    if (vocab.getVocabAreas() != null && !vocab.getVocabAreas().isEmpty()) {
                        var vocabArea = vocab.getVocabAreas().get(0);
                        dto.setVideoUrl(vocabArea.getVocabAreaVideo());
                        dto.setDescription(vocabArea.getVocabAreaDescription());
                        dto.setAreaName(vocabArea.getArea().getAreaName());
                        dto.setCategory(vocabArea.getArea().getAreaName());
                        
                        if (vocab.getSubTopic() != null && vocab.getSubTopic().getSortOrder() != null) {
                            long sortOrder = vocab.getSubTopic().getSortOrder();
                            if (sortOrder <= 3) dto.setDifficulty("easy");
                            else if (sortOrder <= 6) dto.setDifficulty("medium");
                            else dto.setDifficulty("hard");
                        } else {
                            dto.setDifficulty("medium");
                        }
                    }
                    
                    return dto;
                })
                .toList();
            
            return ResponseEntity.ok(vocabularyList);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy vocabulary theo difficulty
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<VocabularyDTO>> getVocabularyByDifficulty(@PathVariable String difficulty) {
        try {
            List<Vocab> vocabList;
            
            // Lọc theo sortOrder để xác định difficulty
            switch (difficulty.toLowerCase()) {
                case "easy":
                    vocabList = vocabRepository.findBySubTopic_SortOrderLessThanEqualAndStatus(3L, "active");
                    break;
                case "medium":
                    vocabList = vocabRepository.findBySubTopic_SortOrderBetweenAndStatus(4L, 6L, "active");
                    break;
                case "hard":
                    vocabList = vocabRepository.findBySubTopic_SortOrderGreaterThanAndStatus(6L, "active");
                    break;
                default:
                    vocabList = vocabRepository.findByStatus("active");
            }
            
            List<VocabularyDTO> vocabularyList = vocabList.stream()
                .map(vocab -> {
                    VocabularyDTO dto = new VocabularyDTO();
                    dto.setId(vocab.getId());
                    dto.setVocab(vocab.getVocab());
                    dto.setMeaning(vocab.getMeaning());
                    
                    if (vocab.getSubTopic() != null) {
                        dto.setSubTopicName(vocab.getSubTopic().getSubTopicName());
                        if (vocab.getSubTopic().getTopic() != null) {
                            dto.setTopicName(vocab.getSubTopic().getTopic().getTopicName());
                        }
                    }
                    
                    if (vocab.getVocabAreas() != null && !vocab.getVocabAreas().isEmpty()) {
                        var vocabArea = vocab.getVocabAreas().get(0);
                        dto.setVideoUrl(vocabArea.getVocabAreaVideo());
                        dto.setDescription(vocabArea.getVocabAreaDescription());
                        dto.setAreaName(vocabArea.getArea().getAreaName());
                        dto.setCategory(vocabArea.getArea().getAreaName());
                        
                        if (vocab.getSubTopic() != null && vocab.getSubTopic().getSortOrder() != null) {
                            long sortOrder = vocab.getSubTopic().getSortOrder();
                            if (sortOrder <= 3) dto.setDifficulty("easy");
                            else if (sortOrder <= 6) dto.setDifficulty("medium");
                            else dto.setDifficulty("hard");
                        } else {
                            dto.setDifficulty("medium");
                        }
                    }
                    
                    return dto;
                })
                .toList();
            
            return ResponseEntity.ok(vocabularyList);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Search vocabulary
    @GetMapping("/search")
    public ResponseEntity<List<VocabularyDTO>> searchVocabulary(@RequestParam String q) {
        try {
            List<Vocab> vocabList = vocabRepository.findByVocabContainingIgnoreCaseAndStatus(q, "active");
            
            List<VocabularyDTO> vocabularyList = vocabList.stream()
                .map(vocab -> {
                    VocabularyDTO dto = new VocabularyDTO();
                    dto.setId(vocab.getId());
                    dto.setMeaning(vocab.getMeaning());
                    dto.setVocab(vocab.getVocab());
                    
                    if (vocab.getSubTopic() != null) {
                        dto.setSubTopicName(vocab.getSubTopic().getSubTopicName());
                        if (vocab.getSubTopic().getTopic() != null) {
                            dto.setTopicName(vocab.getSubTopic().getTopic().getTopicName());
                        }
                    }
                    
                    if (vocab.getVocabAreas() != null && !vocab.getVocabAreas().isEmpty()) {
                        var vocabArea = vocab.getVocabAreas().get(0);
                        dto.setVideoUrl(vocabArea.getVocabAreaVideo());
                        dto.setDescription(vocabArea.getVocabAreaDescription());
                        dto.setAreaName(vocabArea.getArea().getAreaName());
                        dto.setCategory(vocabArea.getArea().getAreaName());
                        
                        if (vocab.getSubTopic() != null && vocab.getSubTopic().getSortOrder() != null) {
                            long sortOrder = vocab.getSubTopic().getSortOrder();
                            if (sortOrder <= 3) dto.setDifficulty("easy");
                            else if (sortOrder <= 6) dto.setDifficulty("medium");
                            else dto.setDifficulty("hard");
                        } else {
                            dto.setDifficulty("medium");
                        }
                    }
                    
                    return dto;
                })
                .toList();
            
            return ResponseEntity.ok(vocabularyList);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 