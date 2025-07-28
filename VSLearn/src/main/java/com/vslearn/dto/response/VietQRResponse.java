package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietQRResponse {
    
    private String qrCodeUrl;        // URL QR code
    private String qrCodeImage;      // Base64 image QR code
    private String transactionCode;  // Mã giao dịch
    private Long amount;             // Số tiền
    private String bankId;           // Mã ngân hàng
    private String accountNo;        // Số tài khoản
    private String accountName;      // Tên chủ tài khoản
    private String description;      // Nội dung chuyển khoản
    private String status;           // Trạng thái
    private String message;          // Thông báo
} 