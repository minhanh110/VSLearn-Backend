package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionResponseDTO {
    private Long id;
    private String type; // "multiple-choice", "true-false", "essay"
    private String videoUrl;
    private String imageUrl;
    private String question;
    private List<String> options; // For multiple choice
    private String correctAnswer;
    private Boolean trueFalseAnswer; // For true/false
    private String essayPrompt; // For essay questions
} 