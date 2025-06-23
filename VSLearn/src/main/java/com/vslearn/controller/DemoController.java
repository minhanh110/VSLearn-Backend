package com.vslearn.controller;

import com.vslearn.dto.response.ResponseData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/demo")
@CrossOrigin(origins = "http://localhost:3000")
public class DemoController {

    @GetMapping("/progress")
    public ResponseEntity<?> getDemoProgress() {
        // Trả về danh sách lesson đã hoàn thành (demo data)
        List<Long> completedLessons = Arrays.asList(1L, 2L); // Demo: lesson 1 và 2 đã hoàn thành
        
        return ResponseEntity.ok(ResponseData.builder()
                .status(200)
                .message("Success")
                .data(completedLessons)
                .build());
    }

    @PostMapping("/progress")
    public ResponseEntity<?> markDemoCompleted(@RequestBody MarkProgressRequest req) {
        // Demo: luôn trả về success
        return ResponseEntity.ok(ResponseData.builder()
                .status(200)
                .message("Marked completed")
                .build());
    }

    public static class MarkProgressRequest {
        private Long lessonId;
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    }
} 