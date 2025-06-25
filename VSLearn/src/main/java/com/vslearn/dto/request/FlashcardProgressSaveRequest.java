package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardProgressSaveRequest {
    private String userId;
    private List<Long> completedFlashcards;
    private Boolean completedPractice;
    private String userChoice; // "continue" | "review"
} 