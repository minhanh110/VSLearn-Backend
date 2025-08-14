package com.vslearn.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class LearningPathDTO {
    private Long unitId;
    private String title;
    private String description;
    private List<LessonDTO> lessons;
    private Boolean accessible = true;
    private String lockReason;
    private Integer wordCount; // Thêm field để lưu tổng số từ vựng của topic
} 