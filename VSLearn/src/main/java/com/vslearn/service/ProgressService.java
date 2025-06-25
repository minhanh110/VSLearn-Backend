package com.vslearn.service;

import org.springframework.http.ResponseEntity;

public interface ProgressService {
    ResponseEntity<?> getProgress(String authHeader);
    ResponseEntity<?> markCompleted(String authHeader, Long lessonId);
    ResponseEntity<?> getDemoProgress();
} 