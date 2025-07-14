package com.vslearn.controller;

import com.vslearn.service.LearningPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/learning-path")
@CrossOrigin(origins = "http://localhost:3000")
public class LearningPathController {
    private final LearningPathService learningPathService;

    @Autowired
    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @GetMapping
    public ResponseEntity<?> getLearningPath(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Debug: Log authentication info
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔍 Authentication: " + authentication);
        System.out.println("🔍 Authorities: " + authentication.getAuthorities());
        System.out.println("🔍 Principal: " + authentication.getPrincipal());
        System.out.println("🔍 Auth Header: " + authHeader);
        
        return learningPathService.getLearningPath(authHeader);
    }

    // Test endpoint để debug
    @GetMapping("/test")
    public ResponseEntity<?> testAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
            "authentication", authentication.toString(),
            "authorities", authentication.getAuthorities().toString(),
            "principal", authentication.getPrincipal().toString()
        ));
    }
} 