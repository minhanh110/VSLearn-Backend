package com.vslearn.service;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;

public interface VietQRService {
    
    /**
     * Tạo VietQR code cho giao dịch
     */
    VietQRResponse createVietQR(VietQRRequest request);
    
    /**
     * Kiểm tra trạng thái thanh toán
     */
    boolean checkPaymentStatus(String transactionCode);
    
    /**
     * Xử lý webhook từ VietQR
     */
    void handleWebhook(String webhookData);
} 