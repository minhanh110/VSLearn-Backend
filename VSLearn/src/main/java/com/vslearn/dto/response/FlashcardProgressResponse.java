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
public class FlashcardProgressResponse {
    private Boolean success;
    private String message;
    private List<Long> completedFlashcards;
    private Boolean completedPractice;
    private String userChoice;
    private Integer progressPercentage;
} 