package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubtopicInfoDTO {
    private Long id;
    private String subTopicName;
    private Long topicId;
    private String topicName;
    private String status;
    private Integer totalFlashcards;
} 