package com.vslearn.service.impl;

import com.vslearn.dto.request.NotifyCreateRequest;
import com.vslearn.dto.response.NotifyResponse;
import com.vslearn.entities.Notify;
import com.vslearn.entities.User;
import com.vslearn.repository.NotifyRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotifyServiceImpl implements NotifyService {
    
    private final NotifyRepository notifyRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public NotifyServiceImpl(NotifyRepository notifyRepository, UserRepository userRepository) {
        this.notifyRepository = notifyRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public NotifyResponse createNotify(NotifyCreateRequest request) {
        // Validate users exist
        Optional<User> fromUser = userRepository.findById(request.getFromUserId());
        Optional<User> toUser = userRepository.findById(request.getToUserId());
        
        if (fromUser.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người gửi với ID: " + request.getFromUserId());
        }
        if (toUser.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người nhận với ID: " + request.getToUserId());
        }
        
        Notify notify = Notify.builder()
                .content(request.getContent())
                .fromUser(fromUser.get())
                .toUser(toUser.get())
                .isSend(false)
                .createdAt(Instant.now())
                .createdBy(request.getFromUserId())
                .build();
        
        Notify savedNotify = notifyRepository.save(notify);
        return convertToNotifyResponse(savedNotify);
    }
    
    @Override
    public NotifyResponse getNotifyById(Long notifyId) {
        Optional<Notify> notify = notifyRepository.findById(notifyId);
        if (notify.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thông báo với ID: " + notifyId);
        }
        return convertToNotifyResponse(notify.get());
    }
    
    @Override
    public Page<NotifyResponse> getNotifiesByToUserId(Long userId, Pageable pageable) {
        Page<Notify> notifies = notifyRepository.findByToUserId(userId, pageable);
        return notifies.map(this::convertToNotifyResponse);
    }
    
    @Override
    public List<NotifyResponse> getUnsentNotifiesByToUserId(Long userId) {
        List<Notify> notifies = notifyRepository.findUnsentByToUserId(userId);
        return notifies.stream()
                .map(this::convertToNotifyResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<NotifyResponse> getSentNotifiesByToUserId(Long userId, Pageable pageable) {
        Page<Notify> notifies = notifyRepository.findSentByToUserId(userId, pageable);
        return notifies.map(this::convertToNotifyResponse);
    }
    
    @Override
    public Long countUnsentNotifiesByToUserId(Long userId) {
        return notifyRepository.countUnsentByToUserId(userId);
    }
    
    @Override
    public void markAsSent(Long notifyId) {
        Optional<Notify> notify = notifyRepository.findById(notifyId);
        if (notify.isPresent()) {
            Notify notifyToUpdate = notify.get();
            notifyToUpdate.setIsSend(true);
            notifyToUpdate.setUpdatedAt(Instant.now());
            notifyRepository.save(notifyToUpdate);
        }
    }
    
    @Override
    public void markAllAsSent(Long userId) {
        notifyRepository.markAllAsSent(userId);
    }
    
    @Override
    public void deleteNotify(Long notifyId) {
        Optional<Notify> notify = notifyRepository.findById(notifyId);
        if (notify.isPresent()) {
            Notify notifyToDelete = notify.get();
            notifyToDelete.setDeletedAt(Instant.now());
            notifyRepository.save(notifyToDelete);
        }
    }
    
    private NotifyResponse convertToNotifyResponse(Notify notify) {
        return NotifyResponse.builder()
                .id(notify.getId())
                .content(notify.getContent())
                .fromUserId(notify.getFromUser().getId())
                .fromUserName(notify.getFromUser().getUserName())
                .toUserId(notify.getToUser().getId())
                .toUserName(notify.getToUser().getUserName())
                .isSend(notify.getIsSend())
                .createdAt(notify.getCreatedAt())
                .updatedAt(notify.getUpdatedAt())
                .build();
    }
} 