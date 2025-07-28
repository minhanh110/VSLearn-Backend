package com.vslearn.service.impl;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;
import com.vslearn.service.VietQRService;
import com.vslearn.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class VietQRServiceImpl implements VietQRService {

    @Value("${vietqr.api.url:https://api.vietqr.io}")
    private String vietqrApiUrl;
    
    @Value("${vietqr.bank.id:VCB}")
    private String defaultBankId;
    
    @Value("${vietqr.account.no:}")
    private String defaultAccountNo;
    
    @Value("${vietqr.account.name:}")
    private String defaultAccountName;
    
    @Value("${vietqr.template:compact}")
    private String defaultTemplate;
    
    @Value("${vietqr.template.id:}")
    private String templateId;

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    private QRCodeService qrCodeService;

    @Override
    public VietQRResponse createVietQR(VietQRRequest request) {
        try {
            // Tạo mã giao dịch nếu chưa có
            if (request.getTransactionCode() == null) {
                request.setTransactionCode("TXN_" + UUID.randomUUID().toString().substring(0, 8));
            }
            
            // Sử dụng thông tin mặc định nếu không có
            if (request.getBankId() == null) {
                request.setBankId(defaultBankId);
            }
            if (request.getAccountNo() == null) {
                request.setAccountNo(defaultAccountNo);
            }
            if (request.getAccountName() == null) {
                request.setAccountName(defaultAccountName);
            }
            if (request.getTemplate() == null) {
                request.setTemplate(defaultTemplate);
            }
            
            // Tạo nội dung chuyển khoản
            if (request.getDescription() == null) {
                request.setDescription(String.format("VSLearn - %s - %s", 
                    request.getPackageName(), 
                    request.getTransactionCode()));
            }
            
            // Gọi API VietQR chính thức với template
            String apiUrl;
            if (templateId != null && !templateId.isEmpty()) {
                // Sử dụng template ID
                apiUrl = String.format("%s/image/970422-113366668888-%s.jpg?amount=%d&content=%s",
                    vietqrApiUrl,
                    templateId,
                    request.getAmount(),
                    request.getDescription()
                );
            } else {
                // Fallback to bank ID
                apiUrl = String.format("%s/image/%s?accountNo=%s&amount=%d&content=%s",
                    vietqrApiUrl,
                    request.getBankId(),
                    request.getAccountNo(),
                    request.getAmount(),
                    request.getDescription()
                );
            }
            
            // Debug log
            System.out.println("Calling VietQR API: " + apiUrl);
            
            try {
                // Thử gọi API VietQR
                String response = restTemplate.getForObject(apiUrl, String.class);
                System.out.println("VietQR API Response: " + response);
                
                // Nếu API thành công, sử dụng response
                if (response != null && !response.contains("error")) {
                    return VietQRResponse.builder()
                            .qrCodeUrl(apiUrl)
                            .qrCodeImage(apiUrl)
                            .transactionCode(request.getTransactionCode())
                            .amount(request.getAmount())
                            .bankId(request.getBankId())
                            .accountNo(request.getAccountNo())
                            .accountName(request.getAccountName())
                            .description(request.getDescription())
                            .status("PENDING")
                            .message("QR code từ VietQR API")
                            .build();
                }
            } catch (Exception e) {
                System.out.println("VietQR API failed, using local QR: " + e.getMessage());
            }
            
            // Fallback: Tạo QR code locally với format đơn giản
            String vietqrContent = String.format("MB|%s|%s|%d|%s",
                request.getAccountNo(),
                request.getAccountName(),
                request.getAmount(),
                request.getDescription()
            );
            
            String qrCodeBase64 = qrCodeService.generateQRCodeBase64(vietqrContent, 300, 300);
            
            System.out.println("Generated local QR content: " + vietqrContent);
            
            return VietQRResponse.builder()
                    .qrCodeUrl(vietqrContent)
                    .qrCodeImage(qrCodeBase64)
                    .transactionCode(request.getTransactionCode())
                    .amount(request.getAmount())
                    .bankId(request.getBankId())
                    .accountNo(request.getAccountNo())
                    .accountName(request.getAccountName())
                    .description(request.getDescription())
                    .status("PENDING")
                    .message("QR code local (fallback)")
                    .build();
                    
        } catch (Exception e) {
            System.err.println("Error creating VietQR: " + e.getMessage());
            e.printStackTrace();
            return VietQRResponse.builder()
                    .status("ERROR")
                    .message("Lỗi tạo QR code: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean checkPaymentStatus(String transactionCode) {
        // TODO: Implement check payment status
        // Có thể kiểm tra qua:
        // 1. Webhook từ ngân hàng
        // 2. API của ngân hàng (nếu có)
        // 3. Manual check qua admin
        return false;
    }

    @Override
    public void handleWebhook(String webhookData) {
        // TODO: Implement webhook handler
        // Parse webhook data và cập nhật trạng thái transaction
        System.out.println("Webhook received: " + webhookData);
    }
    
    /**
     * Tạo VietQR content string theo chuẩn VietQR
     * Format đơn giản: bankId|accountNo|amount|content
     */
    private String generateVietQRContent(VietQRRequest request) {
        // Format VietQR đơn giản để các app ngân hàng có thể nhận diện
        return String.format("%s|%s|%d|%s",
            request.getBankId(),
            request.getAccountNo(),
            request.getAmount(),
            request.getDescription()
        );
    }
    
    /**
     * Tạo format thay thế cho VietQR
     */
    private String generateAlternativeVietQRContent(VietQRRequest request) {
        // Format: bankId:accountNo:amount:content
        return String.format("%s:%s:%d:%s",
            request.getBankId(),
            request.getAccountNo(),
            request.getAmount(),
            request.getDescription()
        );
    }
    
    /**
     * Tạo EMV QR Code format cho thanh toán
     * Format: 00020101021226580014vn.techcombank011001234567890152045000530370454021005802VN6304
     */
    private String generateEMVQRContent(VietQRRequest request) {
        StringBuilder qrContent = new StringBuilder();
        
        // EMV QR Code format
        qrContent.append("000201"); // Payload Format Indicator
        qrContent.append("010212"); // Point of Initiation Method
        qrContent.append("2658"); // Merchant Account Information
        qrContent.append("0014vn.techcombank"); // Global Unique Identifier
        qrContent.append("01").append(String.format("%02d", request.getAccountNo().length())).append(request.getAccountNo()); // Account Number
        qrContent.append("52").append("04").append(request.getBankId()); // Merchant Category Code
        qrContent.append("53").append("03").append("704"); // Transaction Currency (VND)
        qrContent.append("54").append(String.format("%02d", String.valueOf(request.getAmount()).length())).append(request.getAmount()); // Transaction Amount
        qrContent.append("58").append("02").append("VN"); // Country Code
        qrContent.append("63"); // CRC
        qrContent.append("04"); // CRC length
        
        // Calculate CRC (simplified)
        String data = qrContent.toString();
        int crc = calculateCRC(data);
        qrContent.append(String.format("%04X", crc));
        
        return qrContent.toString();
    }
    
    private int calculateCRC(String data) {
        // Simplified CRC calculation
        int crc = 0xFFFF;
        for (char c : data.toCharArray()) {
            crc ^= (c << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFFFF;
    }
} 