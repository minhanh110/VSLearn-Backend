package com.vslearn.service.impl;

import com.vslearn.dto.response.ResponseData;
import com.vslearn.service.DemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {
    @Override
    public ResponseEntity<?> getDemoProgress() {
        List<Long> completedLessons = Arrays.asList(1L, 2L);
        return ResponseEntity.ok(ResponseData.builder()
                .status(200)
                .message("Success")
                .data(completedLessons)
                .build());
    }

    @Override
    public ResponseEntity<?> markDemoCompleted(Long lessonId) {
        return ResponseEntity.ok(ResponseData.builder()
                .status(200)
                .message("Marked completed")
                .build());
    }
} 