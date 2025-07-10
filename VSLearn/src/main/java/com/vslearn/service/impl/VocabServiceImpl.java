package com.vslearn.service.impl;

import com.vslearn.dto.request.VocabCreateRequest;
import com.vslearn.dto.request.VocabUpdateRequest;
import com.vslearn.dto.response.VocabDetailResponse;
import com.vslearn.dto.response.VocabListResponse;
import com.vslearn.entities.Vocab;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.Topic;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VocabServiceImpl implements VocabService {
    private final VocabRepository vocabRepository;
    private final SubTopicRepository subTopicRepository;
    private final TopicRepository topicRepository;

    @Autowired
    public VocabServiceImpl(VocabRepository vocabRepository, SubTopicRepository subTopicRepository, TopicRepository topicRepository) {
        this.vocabRepository = vocabRepository;
        this.subTopicRepository = subTopicRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public VocabListResponse getVocabList(Pageable pageable, String search, String topic, String region) {
        Page<Vocab> vocabPage;
        
        if (search != null && !search.trim().isEmpty()) {
            vocabPage = vocabRepository.findByVocabContainingIgnoreCaseAndDeletedAtIsNull(search, pageable);
        } else if (topic != null && !topic.trim().isEmpty()) {
            vocabPage = vocabRepository.findBySubTopic_Topic_TopicNameContainingIgnoreCaseAndDeletedAtIsNull(topic, pageable);
        } else if (region != null && !region.trim().isEmpty()) {
            vocabPage = vocabRepository.findByRegionContainingIgnoreCaseAndDeletedAtIsNull(region, pageable);
        } else {
            vocabPage = vocabRepository.findByDeletedAtIsNull(pageable);
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
        return convertToVocabDetailResponse(vocab.get());
    }

    @Override
    public VocabDetailResponse createVocab(VocabCreateRequest request) {
        // Validate SubTopic exists
        Optional<SubTopic> subTopic = subTopicRepository.findById(request.getSubTopicId());
        if (subTopic.isEmpty()) {
            throw new RuntimeException("Không tìm thấy SubTopic với ID: " + request.getSubTopicId());
        }
        
        Vocab vocab = Vocab.builder()
                .vocab(request.getVocab())
                .subTopic(subTopic.get())
                .createdAt(Instant.now())
                .createdBy(1L) // TODO: Get from current user
                .build();
        
        Vocab savedVocab = vocabRepository.save(vocab);
        return convertToVocabDetailResponse(savedVocab);
    }

    @Override
    public VocabDetailResponse updateVocab(Long vocabId, VocabUpdateRequest request) {
        Optional<Vocab> existingVocab = vocabRepository.findById(vocabId);
        if (existingVocab.isEmpty()) {
            throw new RuntimeException("Không tìm thấy từ vựng với ID: " + vocabId);
        }
        
        // Validate SubTopic exists
        Optional<SubTopic> subTopic = subTopicRepository.findById(request.getSubTopicId());
        if (subTopic.isEmpty()) {
            throw new RuntimeException("Không tìm thấy SubTopic với ID: " + request.getSubTopicId());
        }
        
        Vocab vocab = existingVocab.get();
        vocab.setVocab(request.getVocab());
        vocab.setSubTopic(subTopic.get());
        vocab.setUpdatedAt(Instant.now());
        vocab.setUpdatedBy(1L); // TODO: Get from current user
        
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
        vocabToDisable.setDeletedAt(Instant.now());
        vocabToDisable.setDeletedBy(1L); // TODO: Get from current user
        
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
        List<Topic> topics = topicRepository.findByDeletedAtIsNull();
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
    public List<Map<String, Object>> getRegions() {
        // Hardcoded regions for now
        return List.of(
                Map.of("id", "toan-quoc", "name", "TOÀN QUỐC"),
                Map.of("id", "mien-bac", "name", "MIỀN BẮC"),
                Map.of("id", "mien-trung", "name", "MIỀN TRUNG"),
                Map.of("id", "mien-nam", "name", "MIỀN NAM")
        );
    }

    private VocabDetailResponse convertToVocabDetailResponse(Vocab vocab) {
        return VocabDetailResponse.builder()
                .id(vocab.getId())
                .vocab(vocab.getVocab())
                .topicName(vocab.getSubTopic() != null && vocab.getSubTopic().getTopic() != null 
                        ? vocab.getSubTopic().getTopic().getTopicName() : null)
                .subTopicName(vocab.getSubTopic() != null ? vocab.getSubTopic().getSubTopicName() : null)
                .status(vocab.getDeletedAt() != null ? "disabled" : "active")
                .createdAt(vocab.getCreatedAt())
                .createdBy(vocab.getCreatedBy())
                .updatedAt(vocab.getUpdatedAt())
                .updatedBy(vocab.getUpdatedBy())
                .deletedAt(vocab.getDeletedAt())
                .deletedBy(vocab.getDeletedBy())
                .build();
    }
} 