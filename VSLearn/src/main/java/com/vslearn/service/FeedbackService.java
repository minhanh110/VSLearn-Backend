package com.vslearn.service;

import com.vslearn.dto.request.FeedbackRequest;
import com.vslearn.dto.response.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    
    /**
     * Submit feedback for a topic
     */
    FeedbackResponse submitFeedback(FeedbackRequest request, Long userId);
    
    /**
     * Get all feedback for a specific topic
     */
    List<FeedbackResponse> getFeedbackByTopicId(Long topicId);
    
    /**
     * Get feedback by user ID
     */
    List<FeedbackResponse> getFeedbackByUserId(Long userId);
    
    /**
     * Get average rating for a topic
     */
    Double getAverageRatingByTopicId(Long topicId);
} 