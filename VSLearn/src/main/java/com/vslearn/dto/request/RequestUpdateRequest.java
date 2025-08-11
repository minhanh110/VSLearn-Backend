package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateRequest {
    private Long assigneeUserId; // optional; default to topic.createdBy if null
    private String message; // optional note from approver
} 