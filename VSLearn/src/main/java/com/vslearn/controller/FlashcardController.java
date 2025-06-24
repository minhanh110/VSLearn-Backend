package com.vslearn.controller;

import com.vslearn.dto.response.FlashcardDTO;
import com.vslearn.dto.response.SubtopicInfoDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.dto.response.TimelineResponseDTO;
import com.vslearn.dto.response.PracticeQuestionsResponseDTO;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.VocabArea;
import com.vslearn.service.FlashcardService;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.VocabAreaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vslearn.dto.response.PracticeQuestionDTO;

@RestController
@RequestMapping("/api/v1/flashcards")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

    // New endpoint: Get timeline for subtopic
    @GetMapping("/subtopic/{subtopicId}/timeline")
    public ResponseEntity<TimelineResponseDTO> getTimeline(@PathVariable String subtopicId) {
        try {
            TimelineResponseDTO timeline = flashcardService.generateTimeline(subtopicId);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // New endpoint: Get practice questions for specific range
    @GetMapping("/subtopic/{subtopicId}/practice")
    public ResponseEntity<PracticeQuestionsResponseDTO> getPracticeQuestions(
            @PathVariable String subtopicId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "3") int end) {
        try {
            PracticeQuestionsResponseDTO questions = flashcardService.generatePracticeQuestions(subtopicId, start, end);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Legacy endpoint: Get all practice questions for subtopic
    @GetMapping("/subtopic/{subtopicId}/practice/all")
    public ResponseEntity<List<PracticeQuestionDTO>> getPracticeQuestionsBySubtopicId(@PathVariable String subtopicId) {
        List<PracticeQuestionDTO> questions = flashcardService.getPracticeQuestionsForSubtopic(subtopicId);
        if (questions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questions);
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