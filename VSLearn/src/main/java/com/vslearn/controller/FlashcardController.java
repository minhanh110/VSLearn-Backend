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
import org.springframework.beans.factory.annotation.Autowired;
import com.vslearn.dto.response.PracticeQuestionDTO;
import com.vslearn.dto.response.FlashcardProgressResponse;
import com.vslearn.dto.request.FlashcardProgressSaveRequest;
import com.vslearn.dto.response.SentenceBuildingQuestionDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.net.URL;



@RestController
@RequestMapping("/api/v1/flashcards")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FlashcardController {
    private final FlashcardService flashcardService;

    @Autowired
    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
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
        return ResponseEntity.ok(flashcardService.getSubtopicInfo(subtopicId));
    }

    @GetMapping("/{areaId}")
    public ResponseEntity<List<FlashcardDTO>> getFlashcardsByAreaId(@PathVariable String areaId) {
        List<FlashcardDTO> flashcards = flashcardService.getFlashcardsForArea(areaId);
        if (flashcards.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(flashcards);
    }

    @GetMapping("/subtopic/{subtopicId}/timeline")
    public ResponseEntity<TimelineResponseDTO> getTimeline(@PathVariable String subtopicId) {
        TimelineResponseDTO timeline = flashcardService.generateTimeline(subtopicId);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/subtopic/{subtopicId}/practice")
    public ResponseEntity<PracticeQuestionsResponseDTO> getPracticeQuestions(
            @PathVariable String subtopicId,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "3") int end) {
        PracticeQuestionsResponseDTO questions = flashcardService.generatePracticeQuestions(subtopicId, start, end);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/subtopic/{subtopicId}/practice/all")
    public ResponseEntity<List<PracticeQuestionDTO>> getPracticeQuestionsBySubtopicId(@PathVariable String subtopicId) {
        List<PracticeQuestionDTO> questions = flashcardService.getPracticeQuestionsForSubtopic(subtopicId);
        if (questions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/subtopic/{subtopicId}/progress")
    public ResponseEntity<FlashcardProgressResponse> saveProgress(
            @PathVariable String subtopicId,
            @RequestBody FlashcardProgressSaveRequest request) {
        FlashcardProgressResponse response = flashcardService.saveProgress(subtopicId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subtopic/{subtopicId}/progress")
    public ResponseEntity<FlashcardProgressResponse> getProgress(
            @PathVariable String subtopicId,
            @RequestParam String userId) {
        FlashcardProgressResponse response = flashcardService.getProgress(subtopicId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subtopic/{subtopicId}/sentence-building")
    public ResponseEntity<List<SentenceBuildingQuestionDTO>> getSentenceBuildingQuestions(
            @PathVariable String subtopicId) {
        List<SentenceBuildingQuestionDTO> questions = flashcardService.getSentenceBuildingQuestions(subtopicId);
        if (questions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/topic/{topicId}/has-sentence-building")
    public ResponseEntity<Map<String, Boolean>> hasSentenceBuildingForTopic(
            @PathVariable Long topicId) {
        boolean hasSentenceBuilding = flashcardService.hasSentenceBuildingForTopic(topicId);
        return ResponseEntity.ok(Map.of("hasSentenceBuilding", hasSentenceBuilding));
    }

    @GetMapping("/topic/{topicId}/sentence-building")
    public ResponseEntity<List<SentenceBuildingQuestionDTO>> getSentenceBuildingQuestionsForTopic(
            @PathVariable Long topicId) {
        List<SentenceBuildingQuestionDTO> questions = flashcardService.getSentenceBuildingQuestionsForTopic(topicId);
        if (questions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(questions);
    }

    //  endpoint debug
    // @GetMapping("/subtopic/{subtopicId}/debug")
    // public ResponseEntity<?> debugSubtopicFlashcards(@PathVariable String subtopicId) {
    //     return ResponseEntity.ok(flashcardService.debugSubtopicFlashcards(subtopicId));
    // }

    // @GetMapping("/test/vocab-subtopic")
    // public ResponseEntity<?> testVocabSubtopicMapping() {
    //     return ResponseEntity.ok(flashcardService.testVocabSubtopicMapping());
    // }

    // @GetMapping("/subtopic/{subtopicId}/word-count")
    // public ResponseEntity<ResponseData<Integer>> getSubtopicWordCount(@PathVariable Long subtopicId) {
    //     return flashcardService.getSubtopicWordCount(subtopicId);
    // }
}