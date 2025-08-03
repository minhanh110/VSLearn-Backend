package com.vslearn.service;

import com.vslearn.dto.response.CassoTransactionResponse;
import com.vslearn.dto.response.CassoAccountResponse;

import java.time.LocalDate;
import java.util.List;

public interface CassoService {
    
    /**
     * Lấy danh sách giao dịch từ Casso API
     */
    List<CassoTransactionResponse.CassoTransaction> getTransactions(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Kiểm tra trạng thái thanh toán dựa trên transaction code và amount
     */
    boolean checkPaymentStatus(String transactionCode, double expectedAmount);
    
    /**
     * Lấy giao dịch theo ID
     */
    CassoTransactionResponse.CassoTransaction getTransactionById(Long transactionId);
    
    /**
     * Lấy danh sách tài khoản ngân hàng
     */
    List<CassoAccountResponse.CassoAccount> getAccounts();
} 