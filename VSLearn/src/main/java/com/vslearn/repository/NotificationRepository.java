package com.vslearn.repository;

import com.vslearn.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy tất cả thông báo của user (đã gửi và chưa gửi)
    @Query("SELECT n FROM Notification n WHERE n.toUser.id = :userId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByToUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Lấy thông báo chưa gửi của user
    @Query("SELECT n FROM Notification n WHERE n.toUser.id = :userId AND n.isSend = false AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUnsentByToUserId(@Param("userId") Long userId);
    
    // Lấy thông báo đã gửi của user
    @Query("SELECT n FROM Notification n WHERE n.toUser.id = :userId AND n.isSend = true AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findSentByToUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Lấy thông báo từ user cụ thể
    @Query("SELECT n FROM Notification n WHERE n.toUser.id = :toUserId AND n.fromUser.id = :fromUserId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findByToUserIdAndFromUserId(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId, Pageable pageable);
    
    // Đếm số thông báo chưa đọc
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.toUser.id = :userId AND n.isSend = false AND n.deletedAt IS NULL")
    Long countUnsentByToUserId(@Param("userId") Long userId);
    
    // Đánh dấu thông báo đã gửi
    @Query("UPDATE Notification n SET n.isSend = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.id = :notificationId")
    void markAsSent(@Param("notificationId") Long notificationId);
    
    // Đánh dấu tất cả thông báo của user đã gửi
    @Query("UPDATE Notification n SET n.isSend = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.toUser.id = :userId AND n.isSend = false")
    void markAllAsSent(@Param("userId") Long userId);
} 