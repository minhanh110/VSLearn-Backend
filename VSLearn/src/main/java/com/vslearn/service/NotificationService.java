package com.vslearn.service;

import com.vslearn.dto.request.NotificationCreateRequest;
import com.vslearn.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationCreateRequest request);
    NotificationResponse getNotificationById(Long notificationId);
    Page<NotificationResponse> getNotificationsByToUserId(Long userId, Pageable pageable);
    List<NotificationResponse> getUnsentNotificationsByToUserId(Long userId);
    Page<NotificationResponse> getSentNotificationsByToUserId(Long userId, Pageable pageable);
    Long countUnsentNotificationsByToUserId(Long userId);
    void markAsSent(Long notificationId);
    void markAllAsSent(Long userId);
    void deleteNotification(Long notificationId);
} 