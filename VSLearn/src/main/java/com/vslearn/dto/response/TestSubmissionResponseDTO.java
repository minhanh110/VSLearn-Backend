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
public class TestSubmissionResponseDTO {
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer score; // Final score (could be existing score if new score is lower)
    private Integer originalScore; // Original score from current test attempt
    private Boolean isPassed; // >= 90%
    private Boolean topicCompleted; // Whether topic is marked as completed
    private Boolean scoreSaved; // Whether the new score was saved (true) or existing score kept (false)
    private NextTopicInfo nextTopic; // Information about next topic if available
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextTopicInfo {
        private Long id;
        private String topicName;
        private Boolean isAvailable;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextSubtopicInfo {
        private Long id;
        private String subtopicName;
        private Long topicId;
        private String topicName;
        private Boolean isAvailable;
    }
} 