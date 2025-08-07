package com.vslearn.repository;

import com.vslearn.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Tìm transaction theo code
     */
    Optional<Transaction> findByCode(String code);
    
    /**
     * Lấy danh sách transaction theo user
     */
    @Query("SELECT t FROM Transaction t WHERE t.createdBy.id = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);
    
    /**
     * Lấy danh sách transaction theo trạng thái thanh toán
     */
    @Query("SELECT t FROM Transaction t WHERE t.paymentStatus = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByPaymentStatus(@Param("status") Transaction.PaymentStatus status);
    
    /**
     * Lấy danh sách transaction chưa thanh toán
     */
    @Query("SELECT t FROM Transaction t WHERE t.paymentStatus = 'PENDING' ORDER BY t.createdAt DESC")
    List<Transaction> findPendingTransactions();
    
    /**
     * Lấy danh sách transaction đã thanh toán
     */
    @Query("SELECT t FROM Transaction t WHERE t.paymentStatus = 'PAID' ORDER BY t.createdAt DESC")
    List<Transaction> findPaidTransactions();
    
    /**
     * Lấy danh sách transaction theo createdBy (Spring Data JPA naming convention)
     */
    List<Transaction> findByCreatedBy_Id(Long userId);
    
    /**
     * Cập nhật payment status của transaction
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.paymentStatus = :status WHERE t.code = :code")
    void updatePaymentStatus(@Param("code") String code, @Param("status") Transaction.PaymentStatus status);
} 