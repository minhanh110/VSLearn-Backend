package com.vslearn.controller;

import com.vslearn.service.LearningPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/learning-path")
@CrossOrigin(origins = "http://localhost:3000")
public class LearningPathController {
    private final LearningPathService learningPathService;

    @Autowired
    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @GetMapping
    public ResponseEntity<?> getLearningPath(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return learningPathService.getLearningPath(authHeader);
    }
} 