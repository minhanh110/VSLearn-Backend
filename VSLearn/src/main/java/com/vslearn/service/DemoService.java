package com.vslearn.service;

import org.springframework.http.ResponseEntity;

public interface DemoService {
    ResponseEntity<?> getDemoProgress();
    ResponseEntity<?> markDemoCompleted(Long lessonId);
} 