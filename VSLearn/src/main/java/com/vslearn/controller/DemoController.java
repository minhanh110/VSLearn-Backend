package com.vslearn.controller;

import com.vslearn.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
@CrossOrigin(origins = "http://localhost:3000")
public class DemoController {
    private final DemoService demoService;

    @Autowired
    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getDemoProgress() {
        return demoService.getDemoProgress();
    }

    @PostMapping("/progress")
    public ResponseEntity<?> markDemoCompleted(@RequestBody MarkProgressRequest req) {
        return demoService.markDemoCompleted(req.getLessonId());
    }

    public static class MarkProgressRequest {
        private Long lessonId;
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    }
} 