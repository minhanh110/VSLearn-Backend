package com.vslearn.service.impl;

import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.Progress;
import com.vslearn.entities.SubTopic;
import com.vslearn.entities.User;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.repository.SubTopicRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.ProgressService;
import com.vslearn.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressServiceImpl implements ProgressService {
    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final SubTopicRepository subTopicRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public ProgressServiceImpl(ProgressRepository progressRepository, UserRepository userRepository, SubTopicRepository subTopicRepository, JwtUtil jwtUtil) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.subTopicRepository = subTopicRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<?> getProgress(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Invalid token - no progress data")
                    .data(List.of())
                    .build());
        }
    }

    @Override
    public ResponseEntity<?> markCompleted(String authHeader, Long lessonId) {
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
            SubTopic subTopic = subTopicRepository.findById(lessonId).orElseThrow();
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

    @Override
    public ResponseEntity<?> getDemoProgress() {
        List<Long> demoCompletedLessons = List.of(1L, 2L);
        return ResponseEntity.ok(ResponseData.builder()
                .status(200)
                .message("Demo progress data")
                .data(demoCompletedLessons)
                .build());
    }
} 