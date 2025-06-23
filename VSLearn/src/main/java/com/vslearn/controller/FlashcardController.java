package com.vslearn.controller;

import com.vslearn.dto.response.FlashcardDTO;
import com.vslearn.dto.response.SubtopicInfoDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.VocabArea;
import com.vslearn.service.FlashcardService;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.VocabAreaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;
    private final SubTopicRepository subTopicRepository;
    private final VocabAreaRepository vocabAreaRepository;

    public FlashcardController(FlashcardService flashcardService, SubTopicRepository subTopicRepository, VocabAreaRepository vocabAreaRepository) {
        this.flashcardService = flashcardService;
        this.subTopicRepository = subTopicRepository;
        this.vocabAreaRepository = vocabAreaRepository;
    }

    @GetMapping("/subtopic/{subtopicId}")
    public ResponseEntity<List<FlashcardDTO>> getFlashcardsBySubtopicId(@PathVariable String subtopicId) {
        List<FlashcardDTO> flashcards = flashcardService.getFlashcardsForSubtopic(subtopicId);
        if (flashcards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/subtopic/{subtopicId}/info")
    public ResponseEntity<SubtopicInfoDTO> getSubtopicInfo(@PathVariable String subtopicId) {
        Optional<SubTopic> subTopic = subTopicRepository.findById(Long.parseLong(subtopicId));
        if (subTopic.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        SubTopic st = subTopic.get();
        SubtopicInfoDTO info = new SubtopicInfoDTO(
            st.getId(),
            st.getSubTopicName(),
            st.getTopic().getTopicName(),
            st.getStatus()
        );
        return ResponseEntity.ok(info);
    }

    // Giữ lại API cũ để tương thích ngược
    @GetMapping("/{areaId}")
    public ResponseEntity<List<FlashcardDTO>> getFlashcardsByAreaId(@PathVariable String areaId) {
        List<FlashcardDTO> flashcards = flashcardService.getFlashcardsForArea(areaId);
        if (flashcards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/subtopic/{subtopicId}/debug")
    public ResponseEntity<?> debugSubtopicFlashcards(@PathVariable String subtopicId) {
        try {
            List<VocabArea> vocabAreas = vocabAreaRepository.findByVocabSubTopicId(Long.parseLong(subtopicId));
            
            List<Object> debugInfo = vocabAreas.stream().map(va -> {
                return Map.of(
                    "vocabAreaId", va.getId(),
                    "vocab", va.getVocab().getVocab(),
                    "subTopicId", va.getVocab().getSubTopic() != null ? va.getVocab().getSubTopic().getId() : "NULL",
                    "subTopicName", va.getVocab().getSubTopic() != null ? va.getVocab().getSubTopic().getSubTopicName() : "NULL",
                    "areaId", va.getArea().getId(),
                    "gifPath", va.getVocabAreaGif()
                );
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "subtopicId", subtopicId,
                "totalFlashcards", vocabAreas.size(),
                "flashcards", debugInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/test/vocab-subtopic")
    public ResponseEntity<?> testVocabSubtopicMapping() {
        try {
            // Test lấy tất cả vocab với subtopic
            List<Object> vocabInfo = vocabAreaRepository.findAll().stream()
                .limit(10) // Chỉ lấy 10 records đầu tiên
                .map(va -> {
                    return Map.of(
                        "vocabAreaId", va.getId(),
                        "vocab", va.getVocab().getVocab(),
                        "subTopicId", va.getVocab().getSubTopic() != null ? va.getVocab().getSubTopic().getId() : "NULL",
                        "subTopicName", va.getVocab().getSubTopic() != null ? va.getVocab().getSubTopic().getSubTopicName() : "NULL"
                    );
                }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "message", "Vocab-Subtopic mapping test",
                "vocabInfo", vocabInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/subtopic/{subtopicId}/word-count")
    public ResponseEntity<ResponseData<Integer>> getSubtopicWordCount(@PathVariable Long subtopicId) {
        try {
            long wordCount = flashcardService.getWordCountBySubtopicId(subtopicId);
            return ResponseEntity.ok(ResponseData.<Integer>builder()
                    .status(200)
                    .message("Word count retrieved successfully")
                    .data((int) wordCount)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.<Integer>builder()
                    .status(500)
                    .message("Error retrieving word count: " + e.getMessage())
                    .data(0)
                    .build());
        }
    }
}