package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardDTO {
    private Long id;
    private FrontDTO front;
    private BackDTO back;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrontDTO {
        private String type;
        private String content;
        private String title;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackDTO {
        private String word;
        private String description;
    }
} 