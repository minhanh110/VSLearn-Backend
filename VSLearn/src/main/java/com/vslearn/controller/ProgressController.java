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
    public ResponseEntity<?> getProgress(@RequestHeader("Authorization") String authHeader) {
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
    }

    @PostMapping
    public ResponseEntity<?> markCompleted(@RequestHeader("Authorization") String authHeader, @RequestBody MarkProgressRequest req) {
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
    }

    public static class MarkProgressRequest {
        private Long lessonId;
        public Long getLessonId() { return lessonId; }
        public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    }
} 