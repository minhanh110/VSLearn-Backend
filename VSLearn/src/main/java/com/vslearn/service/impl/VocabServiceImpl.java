package com.vslearn.service.impl;

import com.vslearn.dto.request.VocabCreateRequest;
import com.vslearn.dto.request.VocabUpdateRequest;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.dto.response.VocabListResponse;
import com.vslearn.dto.response.VideoUploadResponse;
import com.vslearn.dto.response.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vslearn.entities.Vocab;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.Topic;
import com.vslearn.entities.Area;
import com.vslearn.entities.VocabArea;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.AreaRepository;
import com.vslearn.repository.VocabAreaRepository;
import com.vslearn.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

@Service
public class VocabServiceImpl implements VocabService {
    private final VocabRepository vocabRepository;
    private final SubTopicRepository subTopicRepository;
    private final TopicRepository topicRepository;
    private final AreaRepository areaRepository;
    private final VocabAreaRepository vocabAreaRepository;
    private final com.google.cloud.storage.Storage storage;
    private final String bucketName;

    @Autowired
    public VocabServiceImpl(VocabRepository vocabRepository, SubTopicRepository subTopicRepository, TopicRepository topicRepository, AreaRepository areaRepository, VocabAreaRepository vocabAreaRepository,
                          com.google.cloud.storage.Storage storage, @org.springframework.beans.factory.annotation.Value("${gcp.storage.bucket.name}") String bucketName) {
        this.vocabRepository = vocabRepository;
        this.subTopicRepository = subTopicRepository;
        this.topicRepository = topicRepository;
        this.areaRepository = areaRepository;
        this.vocabAreaRepository = vocabAreaRepository;
        this.storage = storage;
        this.bucketName = bucketName;
    }

    // Helper method to get current user ID from security context
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Extract user ID from JWT claims or user details
            // This is a simplified version - you might need to adjust based on your JWT structure
            return 1L; // TODO: Extract actual user ID from JWT claims
        }
        return null;
    }

    // Helper method to check if user can modify vocab
    private boolean canModifyVocab(Vocab vocab, String userRole) {
        if (userRole == null) return false;
        
        // General manager and content approver can modify all vocabs
        if ("ROLE_GENERAL_MANAGER".equals(userRole) || "ROLE_CONTENT_APPROVER".equals(userRole)) {
            return true;
        }
        
        // Content creator can only modify their own vocabs
        if ("ROLE_CONTENT_CREATOR".equals(userRole)) {
            Long currentUserId = getCurrentUserId();
            return currentUserId != null && currentUserId.equals(vocab.getCreatedBy());
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
    public VocabListResponse getVocabList(Pageable pageable, String search, String topic, String region, String letter, String status, Long createdBy) {
        Page<Vocab> vocabPage;
        if (createdBy != null) {
            if (status != null && !status.trim().isEmpty()) {
                vocabPage = vocabRepository.findByStatusAndCreatedByAndDeletedAtIsNull(status, createdBy, pageable);
            } else {
                vocabPage = vocabRepository.findByCreatedByAndDeletedAtIsNull(createdBy, pageable);
            }
        } else {
            // Build combined filter criteria
            boolean hasSearch = search != null && !search.trim().isEmpty();
            boolean hasTopic = topic != null && !topic.trim().isEmpty();
            boolean hasRegion = region != null && !region.trim().isEmpty();
            boolean hasLetter = letter != null && !letter.trim().isEmpty() && !letter.equals("TẤT CẢ");
            boolean hasStatus = status != null && !status.trim().isEmpty();
            
            System.out.println("🔍 Filter criteria - Search: " + hasSearch + " ('" + search + "'), Topic: " + hasTopic + " ('" + topic + "'), Region: " + hasRegion + " ('" + region + "'), Letter: " + hasLetter + " ('" + letter + "'), Status: " + hasStatus + " ('" + status + "')");
            
            try {
                // Use more specific queries for better performance and accuracy
                if (hasSearch) {
                    if (hasTopic && hasRegion && hasLetter && hasStatus) {
                        // Search + Topic + Region + Letter + Status
                        vocabPage = vocabRepository.findByCombinedFiltersWithStatus(search, topic, region, letter, status, pageable);
                    } else if (hasTopic && hasLetter && hasStatus) {
                        // Search + Topic + Letter + Status
                        vocabPage = vocabRepository.findBySearchAndTopicAndStatus(search, topic, status, pageable);
                        vocabPage = vocabPage.getContent().stream()
                            .filter(v -> !hasLetter || v.getVocab().toUpperCase().startsWith(letter.toUpperCase()))
                            .collect(Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new PageImpl<>(list, pageable, list.size())
                            ));
                    } else if (hasTopic && hasStatus) {
                        // Search + Topic + Status
                        vocabPage = vocabRepository.findBySearchAndTopicAndStatus(search, topic, status, pageable);
                    } else if (hasLetter && hasStatus) {
                        // Search + Letter + Status
                        vocabPage = vocabRepository.findBySearchAndLetterAndStatus(search, letter, status, pageable);
                    } else if (hasStatus) {
                        // Search + Status
                        vocabPage = vocabRepository.findBySearchAndStatus(search, status, pageable);
                    } else if (hasTopic && hasLetter) {
                        // Search + Topic + Letter
                        vocabPage = vocabRepository.findBySearchAndTopic(search, topic, pageable);
                        vocabPage = vocabPage.getContent().stream()
                            .filter(v -> !hasLetter || v.getVocab().toUpperCase().startsWith(letter.toUpperCase()))
                            .collect(Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new PageImpl<>(list, pageable, list.size())
                            ));
                    } else if (hasTopic) {
                        // Search + Topic
                        vocabPage = vocabRepository.findBySearchAndTopic(search, topic, pageable);
                    } else if (hasLetter) {
                        // Search + Letter
                        vocabPage = vocabRepository.findBySearchAndLetter(search, letter, pageable);
                    } else {
                        // Search only
                        vocabPage = vocabRepository.findBySearchOnly(search, pageable);
                    }
                    System.out.println("🔍 Found " + vocabPage.getTotalElements() + " vocabularies with search filter");
                } else {
                    // No search term - use combined filters for other filters
                    if (hasStatus && !hasTopic && !hasRegion && !hasLetter) {
                        // Only status filter
                        vocabPage = vocabRepository.findByStatusAndDeletedAtIsNull(status, pageable);
                    } else if (hasTopic || hasRegion || hasLetter || hasStatus) {
                        vocabPage = vocabRepository.findByCombinedFiltersWithStatus(null, topic, region, letter, status, pageable);
                    } else {
                        vocabPage = vocabRepository.findByDeletedAtIsNull(pageable);
                    }
                    System.out.println("🔍 Found " + vocabPage.getTotalElements() + " vocabularies without search filter");
                }
                
                List<VocabDetailResponse> vocabList = vocabPage.getContent().stream()
                        .map(this::convertToVocabDetailResponse)
                        .collect(Collectors.toList());
                
                System.out.println("🔍 Returning " + vocabList.size() + " vocabularies");
                
                return VocabListResponse.builder()
                        .vocabList(vocabList)
                        .currentPage(vocabPage.getNumber())
                        .totalPages(vocabPage.getTotalPages())
                        .totalElements(vocabPage.getTotalElements())
                        .pageSize(vocabPage.getSize())
                        .hasNext(vocabPage.hasNext())
                        .hasPrevious(vocabPage.hasPrevious())
                        .build();
            } catch (Exception e) {
                System.err.println("❌ Error in getVocabList: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        List<VocabDetailResponse> vocabList = vocabPage.getContent().stream()
                .map(this::convertToVocabDetailResponse)
                .collect(Collectors.toList());
        return VocabListResponse.builder()
                .vocabList(vocabList)
                .currentPage(vocabPage.getNumber())
                .totalPages(vocabPage.getTotalPages())
                .totalElements(vocabPage.getTotalElements())
                .pageSize(vocabPage.getSize())
                .hasNext(vocabPage.hasNext())
                .hasPrevious(vocabPage.hasPrevious())
                .build();
    }

    @Override
    public VocabDetailResponse getVocabDetail(Long vocabId) {
        Optional<Vocab> vocab = vocabRepository.findById(vocabId);
        if (vocab.isEmpty()) {
            throw new RuntimeException("Không tìm thấy từ vựng với ID: " + vocabId);
        }
        
        // Load vocab with SubTopic and Topic relationships
        Vocab vocabWithRelations = vocab.get();
        if (vocabWithRelations.getSubTopic() != null) {
            // Force load SubTopic and Topic to avoid LazyInitializationException
            SubTopic subTopic = subTopicRepository.findById(vocabWithRelations.getSubTopic().getId()).orElse(null);
            if (subTopic != null) {
                vocabWithRelations.setSubTopic(subTopic);
            }
        }
        
        return convertToVocabDetailResponse(vocabWithRelations);
    }

    @Override
    public VocabDetailResponse createVocab(VocabCreateRequest request) {
        SubTopic subTopic = null;
        if (request.getSubTopicId() != null) {
            Optional<SubTopic> subTopicOpt = subTopicRepository.findById(request.getSubTopicId());
            if (subTopicOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy SubTopic với ID: " + request.getSubTopicId());
            }
            subTopic = subTopicOpt.get();
        }
        
        // Get or create area
        Area area = null;
        if (request.getRegion() != null && !request.getRegion().trim().isEmpty()) {
            List<Area> areas = areaRepository.findByAreaName(request.getRegion());
            if (!areas.isEmpty()) {
                area = areas.get(0);
            }
        }
        if (area == null) {
            // Default to "Toàn quốc" area
            List<Area> defaultAreas = areaRepository.findByAreaName("Toàn quốc");
            if (!defaultAreas.isEmpty()) {
                area = defaultAreas.get(0);
            }
        }
        
        Vocab vocab = Vocab.builder()
                .vocab(request.getVocab())
                .subTopic(subTopic)
                .createdAt(Instant.now())
                .createdBy(1L) // TODO: Get from current user
                .status("pending") // Content Creator tạo vocab luôn có status pending
                .build();
        
        Vocab savedVocab = vocabRepository.save(vocab);
        
        // Create VocabArea with video and description
        if (area != null) {
            // Prepare description with video metadata if video exists
            String description = request.getDescription();
            if (request.getVideoLink() != null && !request.getVideoLink().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    VideoMetadata videoMetadata = VideoMetadata.builder()
                        .fileName(request.getVideoFileName())
                        .fileSize(request.getVideoSize())
                        .duration(request.getVideoDuration())
                        .contentType(request.getVideoContentType())
                        .uploadedAt(Instant.now())
                        .build();
                    
                    // Create JSON object with description and video metadata
                    Map<String, Object> descriptionWithMetadata = Map.of(
                        "description", description != null ? description : "",
                        "videoMetadata", videoMetadata
                    );
                    
                    description = objectMapper.writeValueAsString(descriptionWithMetadata);
                } catch (Exception e) {
                    // If JSON creation fails, use original description
                    System.err.println("Error creating video metadata JSON: " + e.getMessage());
                }
            }
            
            VocabArea vocabArea = VocabArea.builder()
                    .vocab(savedVocab)
                    .area(area)
                    .vocabAreaVideo(request.getVideoLink())
                    .vocabAreaDescription(description)
                    .createdAt(Instant.now())
                    .createdBy(1L) // TODO: Get from current user
                    .build();
            
            vocabAreaRepository.save(vocabArea);
        }
        
        return convertToVocabDetailResponse(savedVocab);
    }

    @Override
    public VocabDetailResponse updateVocab(Long vocabId, VocabUpdateRequest request) {
        Optional<Vocab> existingVocab = vocabRepository.findById(vocabId);
        if (existingVocab.isEmpty()) {
            throw new RuntimeException("Không tìm thấy từ vựng với ID: " + vocabId);
        }
        
        Vocab vocab = existingVocab.get();
        
        // Kiểm tra quyền chỉnh sửa
        String userRole = getCurrentUserRole();
        if (!canModifyVocab(vocab, userRole)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa từ vựng này");
        }
        
        SubTopic subTopic = null;
        if (request.getSubTopicId() != null) {
            Optional<SubTopic> subTopicOpt = subTopicRepository.findById(request.getSubTopicId());
            if (subTopicOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy SubTopic với ID: " + request.getSubTopicId());
            }
            subTopic = subTopicOpt.get();
        }
        
        vocab.setVocab(request.getVocab());
        vocab.setSubTopic(subTopic);
        vocab.setUpdatedAt(Instant.now());
        vocab.setUpdatedBy(getCurrentUserId());
        vocab.setStatus("pending");
        Vocab savedVocab = vocabRepository.save(vocab);
        return convertToVocabDetailResponse(savedVocab);
    }

    @Override
    public void disableVocab(Long vocabId) {
        Optional<Vocab> vocab = vocabRepository.findById(vocabId);
        if (vocab.isEmpty()) {
            throw new RuntimeException("Không tìm thấy từ vựng với ID: " + vocabId);
        }
        
        Vocab vocabToDisable = vocab.get();
        
        // Kiểm tra quyền xóa
        String userRole = getCurrentUserRole();
        if (!canModifyVocab(vocabToDisable, userRole)) {
            throw new RuntimeException("Bạn không có quyền xóa từ vựng này");
        }
        
        vocabToDisable.setDeletedAt(Instant.now());
        vocabToDisable.setDeletedBy(getCurrentUserId());
        
        vocabRepository.save(vocabToDisable);
    }

    @Override
    public VocabListResponse getRejectedVocabList(Pageable pageable) {
        Page<Vocab> vocabPage = vocabRepository.findByDeletedAtIsNotNull(pageable);
        
        List<VocabDetailResponse> vocabList = vocabPage.getContent().stream()
                .map(this::convertToVocabDetailResponse)
                .collect(Collectors.toList());
        
        return VocabListResponse.builder()
                .vocabList(vocabList)
                .currentPage(vocabPage.getNumber())
                .totalPages(vocabPage.getTotalPages())
                .totalElements(vocabPage.getTotalElements())
                .pageSize(vocabPage.getSize())
                .hasNext(vocabPage.hasNext())
                .hasPrevious(vocabPage.hasPrevious())
                .build();
    }

    @Override
    public List<Map<String, Object>> getTopics() {
        List<Topic> topics = topicRepository.findByStatusAndDeletedAtIsNull("active");
        return topics.stream()
                .map(topic -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", topic.getId());
                    map.put("name", topic.getTopicName());
                    map.put("status", topic.getStatus());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public VocabDetailResponse updateVocabStatus(Long vocabId, String status) {
        Optional<Vocab> existingVocab = vocabRepository.findById(vocabId);
        if (existingVocab.isEmpty()) {
            throw new RuntimeException("Không tìm thấy từ vựng với ID: " + vocabId);
        }
        
        Vocab vocab = existingVocab.get();
        vocab.setStatus(status);
        vocab.setUpdatedAt(Instant.now());
        vocab.setUpdatedBy(1L); // TODO: Get from current user
        
        Vocab savedVocab = vocabRepository.save(vocab);
        return convertToVocabDetailResponse(savedVocab);
    }

    @Override
    public List<Map<String, Object>> getRegions() {
        List<Area> areas = areaRepository.findAll();
        return areas.stream()
            .map(area -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", area.getId());
                map.put("name", area.getAreaName());
                return map;
            })
            .collect(Collectors.toList());
    }

    @Override
    public VideoUploadResponse uploadVideoToGCS(org.springframework.web.multipart.MultipartFile file, String fileName) throws Exception {
        // Generate unique object name
        String objectName = "vocab-videos/" + fileName;
        
        // Upload to GCS
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(file.getContentType())
            .setMetadata(Map.of(
                "originalName", file.getOriginalFilename(),
                "uploadedAt", Instant.now().toString(),
                "fileSize", String.valueOf(file.getSize())
            ))
            .build();
        
        storage.create(blobInfo, file.getBytes());
        
        // Generate signed URL for immediate access
        java.net.URL signedUrl = storage.signUrl(blobInfo, 24, java.util.concurrent.TimeUnit.HOURS, 
            Storage.SignUrlOption.withV4Signature());
        
        return VideoUploadResponse.builder()
            .videoUrl(signedUrl.toString())
            .fileName(fileName)
            .objectName(objectName)
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .metadata(Map.of(
                "bucket", bucketName,
                "objectName", objectName
            ))
            .build();
    }
    
    @Override
    public void deleteVideoFromGCS(String fileName) throws Exception {
        String objectName = "vocab-videos/" + fileName;
        BlobId blobId = BlobId.of(bucketName, objectName);
        storage.delete(blobId);
    }

    private VocabDetailResponse convertToVocabDetailResponse(Vocab vocab) {
        String topicName = null;
        String subTopicName = null;
        String description = null;
        String videoLink = null;
        String region = null;
        
        // Safely get topic and subtopic names
        if (vocab.getSubTopic() != null) {
            subTopicName = vocab.getSubTopic().getSubTopicName();
            
            // Get topic name from SubTopic's Topic relationship
            if (vocab.getSubTopic().getTopic() != null) {
                topicName = vocab.getSubTopic().getTopic().getTopicName();
            } else {
                // Fallback: try to get topic name from repository
                try {
                    SubTopic subTopic = subTopicRepository.findById(vocab.getSubTopic().getId()).orElse(null);
                    if (subTopic != null && subTopic.getTopic() != null) {
                        topicName = subTopic.getTopic().getTopicName();
                    }
                } catch (Exception e) {
                    System.err.println("Error loading topic name for vocab " + vocab.getId() + ": " + e.getMessage());
                }
            }
        }
        
        // Get description, videoLink, and region from vocab_area
        if (vocab.getVocabAreas() != null && !vocab.getVocabAreas().isEmpty()) {
            VocabArea vocabArea = vocab.getVocabAreas().get(0); // Get first vocab area
            description = vocabArea.getVocabAreaDescription();
            
            // Generate signed URL for video like flashcard does
            String objectName = vocabArea.getVocabAreaVideo();
            if (objectName != null && !objectName.trim().isEmpty()) {
                try {
                    com.google.cloud.storage.BlobId blobId = com.google.cloud.storage.BlobId.of(bucketName, objectName);
                    com.google.cloud.storage.BlobInfo blobInfo = com.google.cloud.storage.BlobInfo.newBuilder(blobId).build();
                    
                    java.net.URL signedUrl = storage.signUrl(blobInfo, 2, java.util.concurrent.TimeUnit.HOURS, 
                        com.google.cloud.storage.Storage.SignUrlOption.withV4Signature());
                    
                    videoLink = signedUrl.toString();
                    System.out.println("🔍 Generated signed URL for video: " + videoLink);
                } catch (Exception e) {
                    System.err.println("❌ Error generating signed URL for video: " + e.getMessage());
                    videoLink = objectName; // Fallback to original path
                }
            } else {
                videoLink = objectName;
            }
            
            System.out.println("🔍 VocabArea video: " + videoLink);
            System.out.println("🔍 VocabArea description: " + description);
            if (vocabArea.getArea() != null) {
                region = vocabArea.getArea().getAreaName();
                System.out.println("🔍 VocabArea region: " + region);
            }
        }
        
        return VocabDetailResponse.builder()
                .id(vocab.getId())
                .vocab(vocab.getVocab())
                .topicName(topicName)
                .subTopicName(subTopicName)
                .description(description)
                .videoLink(videoLink)
                .region(region)
                .status(vocab.getStatus())
                .createdAt(vocab.getCreatedAt())
                .createdBy(vocab.getCreatedBy())
                .updatedAt(vocab.getUpdatedAt())
                .updatedBy(vocab.getUpdatedBy())
                .deletedAt(vocab.getDeletedAt())
                .deletedBy(vocab.getDeletedBy())
                .build();
    }
} 