package com.vslearn.controller;

import com.vslearn.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
public class ProgressController {
    private final ProgressService progressService;

    @Autowired
    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ResponseEntity<?> getProgress(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return progressService.getProgress(authHeader);
    }

    @PostMapping
    public ResponseEntity<?> markCompleted(@RequestHeader(value = "Authorization", required = false) String authHeader, @RequestBody MarkProgressRequest req) {
        return progressService.markCompleted(authHeader, req.getLessonId());
    }

    @GetMapping("/demo")
    public ResponseEntity<?> getDemoProgress() {
        return progressService.getDemoProgress();
    }

    public static class MarkProgressRequest {
        private Long lessonId;
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    }
} 