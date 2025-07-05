package com.vslearn.controller;

import com.vslearn.dto.request.FlashcardProgressSaveRequest;
import com.vslearn.dto.response.*;
import com.vslearn.service.FlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.vslearn.entities.Progress;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.entities.SubTopic;
import com.vslearn.repository.SubTopicRepository;

@RestController
@RequestMapping("/api/v1/flashcards")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FlashcardController {
    private final FlashcardService flashcardService;
    private final ProgressRepository progressRepository;
    private final SubTopicRepository subTopicRepository;

    @Autowired
    public FlashcardController(FlashcardService flashcardService, ProgressRepository progressRepository, SubTopicRepository subTopicRepository) {
        this.flashcardService = flashcardService;
        this.progressRepository = progressRepository;
        this.subTopicRepository = subTopicRepository;
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

    @GetMapping("/subtopic/{subtopicId}/next")
    public ResponseEntity<Map<String, Object>> getNextSubtopic(@PathVariable String subtopicId) {
        Map<String, Object> response = flashcardService.getNextSubtopic(subtopicId);
        return ResponseEntity.ok(response);
    }

    // Endpoint để lấy tất cả progress của user
    @GetMapping("/user/progress")
    public ResponseEntity<Map<String, Object>> getUserProgress(@RequestParam String userId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            
            // Lấy tất cả progress của user
            List<Progress> userProgress = progressRepository.findByCreatedBy_Id(userIdLong);
            
            // Lấy danh sách subtopic IDs đã hoàn thành
            List<Long> completedSubtopicIds = userProgress.stream()
                .filter(Progress::getIsComplete)
                .map(p -> p.getSubTopic().getId())
                .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                "userId", userId,
                "totalProgress", userProgress.size(),
                "completedSubtopicIds", completedSubtopicIds,
                "completedCount", completedSubtopicIds.size()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "userId", userId
            ));
        }
    }

    // Endpoint để lấy tất cả subtopics trong topic
    @GetMapping("/topic/{topicId}/subtopics")
    public ResponseEntity<?> getSubtopicsByTopic(@PathVariable Long topicId) {
        try {
            List<SubTopic> subtopics = subTopicRepository.findByTopic_Id(topicId);
            
            List<Map<String, Object>> response = subtopics.stream()
                .map(st -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", st.getId());
                    map.put("name", st.getSubTopicName());
                    map.put("sortOrder", st.getSortOrder());
                    map.put("status", st.getStatus());
                    return map;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // Endpoint debug để kiểm tra tất cả subtopics trong topic
    @GetMapping("/topic/{topicId}/subtopics/debug")
    public ResponseEntity<?> getSubtopicsDebug(@PathVariable Long topicId) {
        try {
            List<SubTopic> subtopics = subTopicRepository.findByTopic_Id(topicId);
            
            List<Map<String, Object>> response = subtopics.stream()
                .map(st -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", st.getId());
                    map.put("name", st.getSubTopicName());
                    map.put("sortOrder", st.getSortOrder());
                    map.put("status", st.getStatus());
                    map.put("deletedAt", st.getDeletedAt());
                    return map;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "topicId", topicId,
                "totalSubtopics", subtopics.size(),
                "subtopics", response
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "topicId", topicId
            ));
        }
    }
}