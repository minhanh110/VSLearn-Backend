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
public class TimelineResponseDTO {
    private List<TimelineStepDTO> timeline;
    private Integer currentPosition;
    private String userId;
    private String subtopicId;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineStepDTO {
        private String type; // "flashcard" | "practice"
        private Integer index;
        private Integer start;
        private Integer end;
    }
} 