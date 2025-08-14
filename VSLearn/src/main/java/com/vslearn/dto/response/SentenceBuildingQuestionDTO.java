package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceBuildingQuestionDTO {
    private Long id;
    private String videoUrl;
    private String imageUrl;
    private String question;
    private String meaning;  // Thêm field meaning
    private String description;  // Thêm field description
    private List<String> words;
    private List<String> correctSentence;
    private String correctAnswer;
} 