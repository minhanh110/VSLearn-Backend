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
public class TestSubmissionRequest {
    private Long userId;
    private Long topicId;
    private List<TestAnswer> answers;
    private Integer score;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestAnswer {
        private Long questionId;
        private String answer;
        private String questionType;
    }
} 