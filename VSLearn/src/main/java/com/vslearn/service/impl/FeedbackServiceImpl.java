package com.vslearn.service.impl;

import com.vslearn.dto.request.FeedbackRequest;
import com.vslearn.dto.response.FeedbackResponse;
import com.vslearn.entities.Topic;
import com.vslearn.entities.TopicPoint;
import com.vslearn.entities.User;
import com.vslearn.repository.TopicPointRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final TopicPointRepository topicPointRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Override
    public FeedbackResponse submitFeedback(FeedbackRequest request, Long userId) {
        log.info("=== Feedback submission started ===");
        log.info("Request: topicId={}, rating={}, feedbackContent={}", 
                request.getTopicId(), request.getRating(), request.getFeedbackContent());
        log.info("UserId: {}", userId);
        
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> {
                    log.error("Topic not found with ID: {}", request.getTopicId());
                    return new RuntimeException("Topic not found");
                });
        log.info("Topic found: {}", topic.getTopicName());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });
        log.info("User found: {}", user.getUserName());
        
        // Check if TopicPoint already exists for this user and topic
        List<TopicPoint> existingTopicPoints = topicPointRepository.findByCreatedByIdAndTopicId(userId, request.getTopicId());
        log.info("Found {} existing TopicPoints for user {} and topic {}", 
                existingTopicPoints.size(), userId, request.getTopicId());
        
        TopicPoint topicPoint;
        
        if (!existingTopicPoints.isEmpty()) {
            // Use the most recent TopicPoint (latest created_at)
            topicPoint = existingTopicPoints.stream()
                    .max(Comparator.comparing(TopicPoint::getCreatedAt))
                    .orElse(existingTopicPoints.get(0));
            log.info("Updating existing TopicPoint with ID: {}", topicPoint.getId());
            
            // Update existing TopicPoint with feedback
            topicPoint.setFeedbackContent(request.getFeedbackContent());
            topicPoint.setRating(request.getRating().longValue());
            topicPoint.setUpdatedAt(Instant.now());
        } else {
            // Create new TopicPoint for feedback
            log.info("Creating new TopicPoint for feedback");
            topicPoint = new TopicPoint();
            topicPoint.setTopic(topic);
            topicPoint.setCreatedBy(user);
            topicPoint.setFeedbackContent(request.getFeedbackContent());
            topicPoint.setRating(request.getRating().longValue());
            topicPoint.setTotalPoint(null); // No test score yet
            topicPoint.setCreatedAt(Instant.now());
            topicPoint.setUpdatedAt(Instant.now());
        }
        
        try {
            topicPoint = topicPointRepository.save(topicPoint);
            log.info("TopicPoint saved successfully with ID: {}", topicPoint.getId());
        } catch (Exception e) {
            log.error("Error saving TopicPoint: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save feedback", e);
        }
        
        FeedbackResponse response = FeedbackResponse.builder()
                .id(topicPoint.getId())
                .topicId(topic.getId())
                .topicName(topic.getTopicName())
                .rating(request.getRating())
                .feedbackContent(request.getFeedbackContent())
                .totalPoint(topicPoint.getTotalPoint())
                .createdBy(user.getUserName())
                .createdAt(topicPoint.getCreatedAt())
                .message("Feedback submitted successfully")
                .build();
        
        log.info("=== Feedback submission completed successfully ===");
        return response;
    }

    @Override
    public List<FeedbackResponse> getFeedbackByTopicId(Long topicId) {
        List<TopicPoint> topicPoints = topicPointRepository.findByTopicId(topicId);
        
        return topicPoints.stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponse> getFeedbackByUserId(Long userId) {
        List<TopicPoint> topicPoints = topicPointRepository.findByCreatedById(userId);
        
        return topicPoints.stream()
                .map(this::mapToFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRatingByTopicId(Long topicId) {
        List<TopicPoint> topicPoints = topicPointRepository.findByTopicId(topicId);
        
        if (topicPoints.isEmpty()) {
            return 0.0;
        }
        
        double totalRating = topicPoints.stream()
                .mapToDouble(tp -> tp.getRating() != null ? tp.getRating() : 0)
                .sum();
        
        return totalRating / topicPoints.size();
    }
    
    private FeedbackResponse mapToFeedbackResponse(TopicPoint topicPoint) {
        return FeedbackResponse.builder()
                .id(topicPoint.getId())
                .topicId(topicPoint.getTopic().getId())
                .topicName(topicPoint.getTopic().getTopicName())
                .rating(topicPoint.getRating() != null ? topicPoint.getRating().intValue() : 0)
                .feedbackContent(topicPoint.getFeedbackContent())
                .totalPoint(topicPoint.getTotalPoint())
                .createdBy(topicPoint.getCreatedBy().getUserName())
                .createdAt(topicPoint.getCreatedAt())
                .build();
    }
} 