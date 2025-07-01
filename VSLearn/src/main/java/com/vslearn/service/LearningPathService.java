package com.vslearn.service;

import org.springframework.http.ResponseEntity;

public interface LearningPathService {
    ResponseEntity<?> getLearningPath(String authHeader);
} 