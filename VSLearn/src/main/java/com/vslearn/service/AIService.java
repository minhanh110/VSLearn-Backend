package com.vslearn.service;

import com.vslearn.dto.AIResponseDTO;
import com.vslearn.dto.VideoProcessingDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AIService {
    
    /**
     * Gửi video đến AI service để xử lý
     * @param videoFile File video cần xử lý
     * @param expectedWord Từ cần nhận diện
     * @param category Danh mục từ
     * @param difficulty Độ khó
     * @return VideoProcessingDTO chứa task ID
     */
    VideoProcessingDTO processVideo(MultipartFile videoFile, String expectedWord, String category, String difficulty);
    
    /**
     * Kiểm tra trạng thái xử lý video
     * @param taskId ID của task
     * @return AIResponseDTO chứa kết quả
     */
    AIResponseDTO checkProcessingStatus(String taskId);
    
    /**
     * Kiểm tra health của AI service
     * @return true nếu AI service hoạt động bình thường
     */
    boolean checkAIServiceHealth();
    
    /**
     * Xóa video từ Google Cloud Storage
     * @param objectName Tên object trong GCS
     */
    void deleteVideoFromGCS(String objectName);

    Map<String, Object> getModelClasses();
} 