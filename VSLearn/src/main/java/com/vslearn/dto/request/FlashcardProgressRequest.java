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
public class FlashcardProgressRequest {
    private String userId;
    private Long flashcardId;
    private String subtopicId;
    private Boolean isCompleted;
    private Integer viewTime; // in seconds
    private Boolean isFlipped;
} 