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
public class UserProgressResponseDTO {
    private String userId;
    private String subtopicId;
    private Integer timelinePosition;
    private List<Long> completedFlashcards;
    private Boolean completedPractice;
    private String userChoice; // "continue" | "review"
    private Integer progressPercentage;
    private Integer totalStudyTime; // in seconds
    private List<FlashcardProgressDTO> flashcardProgress;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashcardProgressDTO {
        private Long flashcardId;
        private Boolean isCompleted;
        private Integer viewCount;
        private Integer totalViewTime;
        private Boolean isFlipped;
    }
} 