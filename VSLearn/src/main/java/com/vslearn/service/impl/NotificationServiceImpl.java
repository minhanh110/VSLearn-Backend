package com.vslearn.service.impl;

import com.vslearn.dto.request.NotificationCreateRequest;
import com.vslearn.dto.response.NotificationResponse;
import com.vslearn.entities.Notification;
import com.vslearn.entities.User;
import com.vslearn.repository.NotificationRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        // Validate users exist
        Optional<User> fromUser = userRepository.findById(request.getFromUserId());
        Optional<User> toUser = userRepository.findById(request.getToUserId());
        
        if (fromUser.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người gửi với ID: " + request.getFromUserId());
        }
        if (toUser.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người nhận với ID: " + request.getToUserId());
        }
        
        Notification notification = Notification.builder()
                .content(request.getContent())
                .fromUser(fromUser.get())
                .toUser(toUser.get())
                .isSend(false)
                .createdAt(Instant.now())
                .createdBy(request.getFromUserId())
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        return convertToNotificationResponse(savedNotification);
    }
    
    @Override
    public NotificationResponse getNotificationById(Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông báo với ID: " + notificationId);
        }
        return convertToNotificationResponse(notification.get());
    }
    
    @Override
    public Page<NotificationResponse> getNotificationsByToUserId(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByToUserId(userId, pageable);
        return notifications.map(this::convertToNotificationResponse);
    }
    
    @Override
    public List<NotificationResponse> getUnsentNotificationsByToUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findUnsentByToUserId(userId);
        return notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<NotificationResponse> getSentNotificationsByToUserId(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findSentByToUserId(userId, pageable);
        return notifications.map(this::convertToNotificationResponse);
    }
    
    @Override
    public Long countUnsentNotificationsByToUserId(Long userId) {
        return notificationRepository.countUnsentByToUserId(userId);
    }
    
    @Override
    public void markAsSent(Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            Notification notificationToUpdate = notification.get();
            notificationToUpdate.setIsSend(true);
            notificationToUpdate.setUpdatedAt(Instant.now());
            notificationRepository.save(notificationToUpdate);
        }
    }
    
    @Override
    public void markAllAsSent(Long userId) {
        notificationRepository.markAllAsSent(userId);
    }
    
    @Override
    public void deleteNotification(Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            Notification notificationToDelete = notification.get();
            notificationToDelete.setDeletedAt(Instant.now());
            notificationRepository.save(notificationToDelete);
        }
    }
    
    private NotificationResponse convertToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .fromUserId(notification.getFromUser().getId())
                .fromUserName(notification.getFromUser().getUserName())
                .toUserId(notification.getToUser().getId())
                .toUserName(notification.getToUser().getUserName())
                .isSend(notification.getIsSend())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
} 