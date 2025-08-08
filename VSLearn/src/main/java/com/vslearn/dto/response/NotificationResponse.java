package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String content;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Boolean isSend;
    private Instant createdAt;
    private Instant updatedAt;
} 