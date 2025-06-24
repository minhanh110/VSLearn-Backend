package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PracticeResultRequest {
    private String userId;
    private String subtopicId;
    private Long flashcardId;
    private String questionType; // "multiple_choice", "matching", etc.
    private Boolean isCorrect;
    private Integer responseTime; // in milliseconds
    private String selectedAnswer;
    private String correctAnswer;
} 