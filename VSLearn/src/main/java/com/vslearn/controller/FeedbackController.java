package com.vslearn.controller;

import com.vslearn.dto.request.FeedbackRequest;
import com.vslearn.dto.response.FeedbackResponse;
import com.vslearn.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Submit feedback for a topic
     */
    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @RequestParam Long userId) {
        
        FeedbackResponse response = feedbackService.submitFeedback(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all feedback for a specific topic
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByTopicId(@PathVariable Long topicId) {
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByTopicId(topicId);
        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Get feedback by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByUserId(@PathVariable Long userId) {
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByUserId(userId);
        return ResponseEntity.ok(feedbackList);
    }

    /**
     * Get average rating for a topic
     */
    @GetMapping("/topic/{topicId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByTopicId(@PathVariable Long topicId) {
        Double averageRating = feedbackService.getAverageRatingByTopicId(topicId);
        return ResponseEntity.ok(averageRating);
    }
} 