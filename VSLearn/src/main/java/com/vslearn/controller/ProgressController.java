package com.vslearn.controller;

import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.Progress;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.User;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubTopicRepository subTopicRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getProgress(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Nếu không có Authorization header, trả về dữ liệu demo cho guest user
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Trả về danh sách rỗng cho guest user
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Guest user - no progress data")
                    .data(List.of())
                    .build());
        }
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = Long.parseLong((String) jwtUtil.getClaimsFromToken(token).getClaims().get("id"));
            List<Progress> progresses = progressRepository.findByCreatedBy_Id(userId);
            List<Long> completedLessonIds = progresses.stream()
                    .filter(Progress::getIsComplete)
                    .map(p -> p.getSubTopic().getId())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Success")
                    .data(completedLessonIds)
                    .build());
        } catch (Exception e) {
            // Nếu có lỗi với token, trả về dữ liệu rỗng
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Invalid token - no progress data")
                    .data(List.of())
                    .build());
        }
    }

    @PostMapping
    public ResponseEntity<?> markCompleted(@RequestHeader(value = "Authorization", required = false) String authHeader, @RequestBody MarkProgressRequest req) {
        // Nếu không có Authorization header, trả về lỗi
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(401)
                    .message("Authentication required")
                    .data(null)
                    .build());
        }
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = Long.parseLong((String) jwtUtil.getClaimsFromToken(token).getClaims().get("id"));
            User user = userRepository.findById(userId).orElseThrow();
            SubTopic subTopic = subTopicRepository.findById(req.getLessonId()).orElseThrow();

            Progress progress = new Progress();
            progress.setCreatedBy(user);
            progress.setSubTopic(subTopic);
            progress.setIsComplete(true);
            progress.setCreatedAt(Instant.now());
            progressRepository.save(progress);

            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Marked completed")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Error marking progress: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/demo")
    public ResponseEntity<?> getDemoProgress() {
        // Trả về dữ liệu demo cho guest user
        List<Long> demoCompletedLessons = List.of(1L, 2L); // Demo: hoàn thành lesson 1 và 2
        return ResponseEntity.ok(ResponseData.builder()
                .status(200)
                .message("Demo progress data")
                .data(demoCompletedLessons)
                .build());
    }

    public static class MarkProgressRequest {
        private Long lessonId;
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    }
} 