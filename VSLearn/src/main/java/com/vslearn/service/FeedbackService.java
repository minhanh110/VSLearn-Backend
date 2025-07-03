package com.vslearn.service;

import com.vslearn.dto.request.FeedbackRequest;
import com.vslearn.dto.response.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    FeedbackResponse submitFeedback(FeedbackRequest request, Long userId);
 
    List<FeedbackResponse> getFeedbackByTopicId(Long topicId);
    
    List<FeedbackResponse> getFeedbackByUserId(Long userId);

    Double getAverageRatingByTopicId(Long topicId);
} 