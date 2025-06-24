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
public class PracticeQuestionsResponseDTO {
    private List<PracticeQuestionDTO> questions;
    private Integer start;
    private Integer end;
    private String subtopicId;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PracticeQuestionDTO {
        private Long id;
        private String videoUrl;
        private String imageUrl;
        private String question;
        private List<OptionDTO> options;
        private String correctAnswer;
        
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OptionDTO {
            private String text;
            private String videoUrl;
            private String imageUrl;
        }
    }
} 