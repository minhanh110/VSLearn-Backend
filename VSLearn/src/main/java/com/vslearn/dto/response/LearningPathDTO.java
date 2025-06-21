package com.vslearn.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class LearningPathDTO {
    private Long unitId;
    private String title;
    private String description;
    private List<LessonDTO> lessons;
} 