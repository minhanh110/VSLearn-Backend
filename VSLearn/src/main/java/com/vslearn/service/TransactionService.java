package com.vslearn.service;

import com.vslearn.entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    
    /**
     * Tạo transaction mới khi user bắt đầu thanh toán
     */
    Transaction createTransaction(String transactionCode, Double amount, String description);
    
    /**
     * Tạo transaction mới với pricing ID cụ thể
     */
    Transaction createTransaction(String transactionCode, Double amount, String description, Long pricingId);
    
    /**
     * Tạo transaction mới với pricing ID và user ID cụ thể
     */
    Transaction createTransaction(String transactionCode, Double amount, String description, Long pricingId, Long userId);
    
    /**
     * Cập nhật trạng thái thanh toán
     */
    Transaction updatePaymentStatus(String transactionCode, Transaction.PaymentStatus status);
    
    /**
     * Tìm transaction theo code
     */
    Optional<Transaction> findByCode(String transactionCode);
    
    /**
     * Lấy danh sách transaction theo user
     */
    List<Transaction> findByUserId(Long userId);
    
    /**
     * Lấy danh sách transaction theo trạng thái
     */
    List<Transaction> findByPaymentStatus(Transaction.PaymentStatus status);
    
    /**
     * Check xem transaction đã thanh toán chưa
     */
    boolean isTransactionPaid(String transactionCode);
} 