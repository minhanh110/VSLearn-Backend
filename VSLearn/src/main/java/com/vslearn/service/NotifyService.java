package com.vslearn.service;

import com.vslearn.dto.request.NotifyCreateRequest;
import com.vslearn.dto.response.NotifyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotifyService {
    NotifyResponse createNotify(NotifyCreateRequest request);
    NotifyResponse getNotifyById(Long notifyId);
    Page<NotifyResponse> getNotifiesByToUserId(Long userId, Pageable pageable);
    List<NotifyResponse> getUnsentNotifiesByToUserId(Long userId);
    Page<NotifyResponse> getSentNotifiesByToUserId(Long userId, Pageable pageable);
    Long countUnsentNotifiesByToUserId(Long userId);
    void markAsSent(Long notifyId);
    void markAllAsSent(Long userId);
    void deleteNotify(Long notifyId);
} 