package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietQRRequest {
    
    private String bankId;           // Mã ngân hàng (VD: VCB, TCB, BIDV)
    private String accountNo;        // Số tài khoản nhận tiền
    private String accountName;      // Tên chủ tài khoản
    private Long amount;             // Số tiền (VND)
    private String transactionCode;  // Mã giao dịch
    private String description;      // Nội dung chuyển khoản
    private String template;         // Template QR (optional)
    
    // Thông tin gói học
    private String packageName;
    private String packageDuration;
    private Long userId;
} 