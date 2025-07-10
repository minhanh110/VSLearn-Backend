package com.vslearn.service.impl;

import com.vslearn.dto.response.AdminDashboardResponse;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.VocabRepository;
import com.vslearn.repository.ProgressRepository;
import com.vslearn.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final VocabRepository vocabRepository;
    private final ProgressRepository progressRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, TopicRepository topicRepository, 
                          VocabRepository vocabRepository, ProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.vocabRepository = vocabRepository;
        this.progressRepository = progressRepository;
    }

    @Override
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalTopics = topicRepository.count();
        long totalVocab = vocabRepository.count();
        long totalProgress = progressRepository.count();
        
        // TODO: Implement more detailed stats
        long activeUsers = totalUsers; // For now, assume all users are active
        long completedLessons = totalProgress; // For now, assume all progress is completed lessons
        long totalTests = 0; // TODO: Implement test counting
        long completedTests = 0; // TODO: Implement completed test counting
        
        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalTopics(totalTopics)
                .totalVocab(totalVocab)
                .totalProgress(totalProgress)
                .completedLessons(completedLessons)
                .totalTests(totalTests)
                .completedTests(completedTests)
                .build();
    }

    @Override
    public Map<String, Object> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = totalUsers; // TODO: Implement active user counting
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("newUsersThisMonth", 0); // TODO: Implement
        stats.put("newUsersThisWeek", 0); // TODO: Implement
        
        return stats;
    }

    @Override
    public Map<String, Object> getContentStats() {
        long totalTopics = topicRepository.count();
        long totalVocab = vocabRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTopics", totalTopics);
        stats.put("totalVocab", totalVocab);
        stats.put("activeTopics", totalTopics); // TODO: Implement active topic counting
        stats.put("activeVocab", totalVocab); // TODO: Implement active vocab counting
        
        return stats;
    }

    @Override
    public Map<String, Object> getLearningStats() {
        long totalProgress = progressRepository.count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProgress", totalProgress);
        stats.put("completedLessons", totalProgress); // TODO: Implement completed lesson counting
        stats.put("averageCompletionRate", 0.0); // TODO: Implement
        stats.put("mostPopularTopic", "N/A"); // TODO: Implement
        
        return stats;
    }
} 