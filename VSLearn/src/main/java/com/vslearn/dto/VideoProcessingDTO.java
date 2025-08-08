package com.vslearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoProcessingDTO {
    private String taskId;
    private String status;
    private String message;
    private String expectedWord; // Từ cần nhận diện
    private String category;     // Danh mục từ
    private String difficulty;   // Độ khó
} 