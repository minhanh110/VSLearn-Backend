package com.vslearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyCreateRequest {
    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String content;
    
    @NotNull(message = "ID người gửi không được để trống")
    private Long fromUserId;
    
    @NotNull(message = "ID người nhận không được để trống")
    private Long toUserId;
} 