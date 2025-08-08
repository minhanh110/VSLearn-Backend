package com.vslearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyDTO {
    private Long id;
    private String vocab; // Tên từ vựng
    private String meaning; // Nghĩa của từ
    private String category; // Area name hoặc SubTopic name
    private String videoUrl; // VocabArea video URL
    private String description; // VocabArea description
    private String difficulty; // Có thể tính từ sortOrder hoặc area
    private String subTopicName;
    private String topicName;
    private String areaName;
} 