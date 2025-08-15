package com.vslearn.service.impl;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.vslearn.dto.request.SentenceCreateRequest;
import com.vslearn.dto.request.SentenceUpdateRequest;
import com.vslearn.dto.response.SentenceDetailResponse;
import com.vslearn.dto.response.SentenceListResponse;
import com.vslearn.dto.response.VideoUploadResponse;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.dto.response.TopicDetailResponse;
import com.vslearn.entities.Sentence;
import com.vslearn.entities.SentenceVocab;
import com.vslearn.entities.Topic;
import com.vslearn.entities.Vocab;
import com.vslearn.repository.SentenceRepository;
import com.vslearn.repository.SentenceVocabRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.service.SentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SentenceServiceImpl implements SentenceService {

    @Autowired
    private SentenceRepository sentenceRepository;

    @Autowired
    private SentenceVocabRepository sentenceVocabRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private VocabRepository vocabRepository;

    @Autowired
    private Storage storage;

    @Value("${gcp.storage.bucket.name}")
    private String bucketName;

    @Override
    public SentenceDetailResponse createSentence(SentenceCreateRequest request) {
        // Validate topic exists
        Optional<Topic> topicOpt = topicRepository.findById(request.getTopicId());
        if (topicOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy Topic với ID: " + request.getTopicId());
        }
        Topic topic = topicOpt.get();

        // Get current user ID
        Long currentUserId = getCurrentUserId();

        // Create sentence
        Sentence sentence = Sentence.builder()
                .sentenceVideo(request.getSentenceVideo()) // This will be objectName, not full URL
                .sentenceMeaning(request.getSentenceMeaning())
                .sentenceDescription(request.getSentenceDescription())
                .sentenceTopic(topic)
                .createdAt(Instant.now())
                .createdBy(currentUserId)
                .build();

        // Set parent if provided
        if (request.getParentId() != null) {
            Optional<Sentence> parentOpt = sentenceRepository.findById(request.getParentId());
            if (parentOpt.isPresent()) {
                sentence.setParent(parentOpt.get());
            }
        }

        Sentence savedSentence = sentenceRepository.save(sentence);

        // Create sentence_vocab relationships if vocabIds provided
        if (request.getVocabIds() != null && !request.getVocabIds().isEmpty()) {
            for (Long vocabId : request.getVocabIds()) {
                Optional<Vocab> vocabOpt = vocabRepository.findById(vocabId);
                if (vocabOpt.isPresent()) {
                    SentenceVocab sentenceVocab = SentenceVocab.builder()
                            .sentence(savedSentence)
                            .vocab(vocabOpt.get())
                            .createdAt(Instant.now())
                            .createdBy(currentUserId)
                            .build();
                    sentenceVocabRepository.save(sentenceVocab);
                }
            }
        }

        return convertToSentenceDetailResponse(savedSentence);
    }

    @Override
    public SentenceDetailResponse updateSentence(Long sentenceId, SentenceUpdateRequest request) {
        Optional<Sentence> sentenceOpt = sentenceRepository.findById(sentenceId);
        if (sentenceOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy Sentence với ID: " + sentenceId);
        }

        Sentence sentence = sentenceOpt.get();
        Long currentUserId = getCurrentUserId();

        // Update basic fields
        if (request.getSentenceVideo() != null) {
            sentence.setSentenceVideo(request.getSentenceVideo());
        }
        if (request.getSentenceMeaning() != null) {
            sentence.setSentenceMeaning(request.getSentenceMeaning());
        }
        if (request.getSentenceDescription() != null) {
            sentence.setSentenceDescription(request.getSentenceDescription());
        }

        // Update topic if provided
        if (request.getTopicId() != null) {
            Optional<Topic> topicOpt = topicRepository.findById(request.getTopicId());
            if (topicOpt.isPresent()) {
                sentence.setSentenceTopic(topicOpt.get());
            }
        }

        // Update parent if provided
        if (request.getParentId() != null) {
            Optional<Sentence> parentOpt = sentenceRepository.findById(request.getParentId());
            if (parentOpt.isPresent()) {
                sentence.setParent(parentOpt.get());
            }
        }

        sentence.setUpdatedAt(Instant.now());
        sentence.setUpdatedBy(currentUserId);

        Sentence updatedSentence = sentenceRepository.save(sentence);

        // Update sentence_vocab relationships if vocabIds provided
        if (request.getVocabIds() != null) {
            // Delete existing relationships
            List<SentenceVocab> existingVocabs = sentenceVocabRepository.findBySentenceId(sentenceId);
            sentenceVocabRepository.deleteAll(existingVocabs);

            // Create new relationships
            for (Long vocabId : request.getVocabIds()) {
                Optional<Vocab> vocabOpt = vocabRepository.findById(vocabId);
                if (vocabOpt.isPresent()) {
                    SentenceVocab sentenceVocab = SentenceVocab.builder()
                            .sentence(updatedSentence)
                            .vocab(vocabOpt.get())
                            .createdAt(Instant.now())
                            .createdBy(currentUserId)
                            .build();
                    sentenceVocabRepository.save(sentenceVocab);
                }
            }
        }

        return convertToSentenceDetailResponse(updatedSentence);
    }

    @Override
    public SentenceDetailResponse getSentenceDetail(Long sentenceId) {
        Optional<Sentence> sentenceOpt = sentenceRepository.findById(sentenceId);
        if (sentenceOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy Sentence với ID: " + sentenceId);
        }
        return convertToSentenceDetailResponse(sentenceOpt.get());
    }

    @Override
    public SentenceListResponse getSentenceList(Pageable pageable, String search, String topic, String status, Long createdBy) {
        // For now, return all sentences. You can implement search/filter logic later
        Page<Sentence> sentencePage = sentenceRepository.findAll(pageable);
        
        List<SentenceDetailResponse> sentenceResponses = sentencePage.getContent().stream()
                .map(this::convertToSentenceDetailResponse)
                .collect(Collectors.toList());

        return SentenceListResponse.builder()
                .sentenceList(sentenceResponses)
                .totalElements(sentencePage.getTotalElements())
                .totalPages(sentencePage.getTotalPages())
                .currentPage(sentencePage.getNumber())
                .pageSize(sentencePage.getSize())
                .build();
    }

    @Override
    public Map<String, Object> deleteSentence(Long sentenceId) {
        Optional<Sentence> sentenceOpt = sentenceRepository.findById(sentenceId);
        if (sentenceOpt.isEmpty()) {
            return Map.of("success", false, "message", "Không tìm thấy Sentence với ID: " + sentenceId);
        }

        Sentence sentence = sentenceOpt.get();
        Long currentUserId = getCurrentUserId();

        // Soft delete
        sentence.setDeletedAt(Instant.now());
        sentence.setDeletedBy(currentUserId);
        sentenceRepository.save(sentence);

        return Map.of("success", true, "message", "Xóa sentence thành công");
    }

    @Override
    public VideoUploadResponse uploadVideoToGCS(MultipartFile file, String fileName) throws Exception {
        // Generate unique object name
        String objectName = "sentence-videos/" + fileName;

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

        // Generate signed URL
        URL signedUrl = storage.signUrl(blobInfo, 2, java.util.concurrent.TimeUnit.HOURS,
                Storage.SignUrlOption.withV4Signature());

        return VideoUploadResponse.builder()
                .videoUrl(signedUrl.toString())
                .objectName(objectName)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    @Override
    public List<SentenceDetailResponse> getSentencesByTopicId(Long topicId) {
        List<Sentence> sentences = sentenceRepository.findBySentenceTopicId(topicId);
        return sentences.stream()
                .map(this::convertToSentenceDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTopicId(Long topicId) {
        return sentenceRepository.existsBySentenceTopicId(topicId);
    }

    private SentenceDetailResponse convertToSentenceDetailResponse(Sentence sentence) {
        // Get vocabs for this sentence
        List<SentenceVocab> sentenceVocabs = sentenceVocabRepository.findBySentenceId(sentence.getId());
        List<VocabDetailResponse> vocabResponses = sentenceVocabs.stream()
                .map(sv -> VocabDetailResponse.builder()
                        .id(sv.getVocab().getId())
                        .vocab(sv.getVocab().getVocab())
                        .description(sv.getVocab().getMeaning())
                        .build())
                .collect(Collectors.toList());

        // Get parent sentence if exists
        SentenceDetailResponse parentResponse = null;
        if (sentence.getParent() != null) {
            parentResponse = convertToSentenceDetailResponse(sentence.getParent());
        }

        return SentenceDetailResponse.builder()
                .id(sentence.getId())
                .sentenceVideo(sentence.getSentenceVideo())
                .sentenceMeaning(sentence.getSentenceMeaning())
                .sentenceDescription(sentence.getSentenceDescription())
                .topic(TopicDetailResponse.builder()
                        .id(sentence.getSentenceTopic().getId())
                        .topicName(sentence.getSentenceTopic().getTopicName())
                        .build())
                .vocabs(vocabResponses)
                .parent(parentResponse)
                .createdAt(sentence.getCreatedAt())
                .createdBy(sentence.getCreatedBy())
                .updatedAt(sentence.getUpdatedAt())
                .updatedBy(sentence.getUpdatedBy())
                .build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            // You might need to implement user ID extraction based on your authentication setup
            return 1L; // Default user ID for now
        }
        return 1L; // Default user ID
    }
} 