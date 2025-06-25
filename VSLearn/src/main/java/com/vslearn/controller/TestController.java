package com.vslearn.controller;

import com.vslearn.dto.request.TestSubmissionRequest;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.dto.response.TestQuestionResponseDTO;
import com.vslearn.dto.response.TestSubmissionResponseDTO;
import com.vslearn.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TestController {

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/generate")
    public ResponseEntity<ResponseData<List<TestQuestionResponseDTO>>> generateTest(
            @RequestParam Long userId,
            @RequestParam Long topicId) {
        return testService.generateTest(userId, topicId);
    }


    @PostMapping("/submit")
    public ResponseEntity<ResponseData<TestSubmissionResponseDTO>> submitTest(
            @RequestBody TestSubmissionRequest request) {
        return testService.submitTest(request);
    }


    @GetMapping("/next-topic")
    public ResponseEntity<ResponseData<TestSubmissionResponseDTO.NextTopicInfo>> getNextTopic(
            @RequestParam Long userId,
            @RequestParam Long currentTopicId) {
        return testService.getNextTopic(userId, currentTopicId);
    }

    @GetMapping("/next-subtopic")
    public ResponseEntity<ResponseData<TestSubmissionResponseDTO.NextSubtopicInfo>> getNextSubtopic(
            @RequestParam Long topicId) {
        return testService.getNextSubtopic(topicId);
    }

    @GetMapping("/topic-name")
    public ResponseEntity<ResponseData<String>> getTopicName(@RequestParam Long topicId) {
        return testService.getTopicName(topicId);
    }
} 