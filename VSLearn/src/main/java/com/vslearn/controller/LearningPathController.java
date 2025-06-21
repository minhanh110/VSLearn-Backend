package com.vslearn.controller;

import com.vslearn.dto.response.LearningPathDTO;
import com.vslearn.dto.response.LessonDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.Topic;
import com.vslearn.entities.SubTopic;
import com.vslearn.repository.TopicRepository;
import com.vslearn.repository.SubTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/learning-path")
public class LearningPathController {

    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private SubTopicRepository subTopicRepository;

    @GetMapping
    public ResponseEntity<?> getLearningPath() {
        try {
            // Log để debug
            System.out.println("=== LearningPathController.getLearningPath() called ===");
            
            List<Topic> topics = topicRepository.findAll();
            System.out.println("Found " + topics.size() + " topics in database");
            
            if (topics.isEmpty()) {
                System.out.println("WARNING: No topics found in database!");
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No topics found in database")
                        .data(new ArrayList<>())
                        .build());
            }
            
            List<LearningPathDTO> result = new ArrayList<>();

            for (Topic topic : topics) {
                System.out.println("Processing topic: " + topic.getTopicName() + " (ID: " + topic.getId() + ")");
                
                LearningPathDTO dto = new LearningPathDTO();
                dto.setUnitId(topic.getId());
                dto.setTitle(topic.getTopicName());
                
                // Tạo description từ topic name hoặc có thể lấy từ database
                dto.setDescription("Học " + topic.getTopicName().toLowerCase());

                List<SubTopic> subTopics = subTopicRepository.findByTopic_Id(topic.getId());
                System.out.println("Found " + subTopics.size() + " sub-topics for topic " + topic.getId());
                
                List<LessonDTO> lessons = subTopics.stream().map(sub -> {
                    LessonDTO l = new LessonDTO();
                    l.setId(sub.getId());
                    l.setTitle(sub.getSubTopicName());
                    l.setIsTest(sub.getStatus() != null && sub.getStatus().toLowerCase().contains("test"));
                    
                    // Set default values cho wordCount và questionCount
                    if (l.getIsTest()) {
                        l.setQuestionCount(10); // Default 10 câu hỏi cho test
                        l.setWordCount(null);
                    } else {
                        l.setWordCount(20); // Default 20 từ cho lesson
                        l.setQuestionCount(null);
                    }
                    
                    System.out.println("  - Lesson: " + l.getTitle() + " (ID: " + l.getId() + ", isTest: " + l.getIsTest() + ")");
                    return l;
                }).collect(Collectors.toList());

                dto.setLessons(lessons);
                result.add(dto);
            }

            System.out.println("Returning " + result.size() + " units");
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Success")
                    .data(result)
                    .build());
                    
        } catch (Exception e) {
            System.err.println("ERROR in getLearningPath: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Error: " + e.getMessage())
                    .data(new ArrayList<>())
                    .build());
        }
    }
} 