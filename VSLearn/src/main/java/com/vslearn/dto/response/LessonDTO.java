package com.vslearn.dto.response;

import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private Boolean isTest;
    private Integer wordCount; // Số từ trong lesson
    private Integer questionCount; // Số câu hỏi trong test
    private Boolean accessible = true;
    private Boolean isCompleted = false; // Lesson đã hoàn thành hay chưa
} 