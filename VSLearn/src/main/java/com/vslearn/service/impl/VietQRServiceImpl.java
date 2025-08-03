package com.vslearn.service.impl;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;
import com.vslearn.entities.Transaction;
import com.vslearn.service.VietQRService;
import com.vslearn.service.QRCodeService;
import com.vslearn.service.CassoService;
import com.vslearn.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.vslearn.utils.JwtUtil;

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
    
    @Autowired
    private CassoService cassoService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public VietQRResponse createVietQR(VietQRRequest request) {
        try {
            // Tạo mã giao dịch nếu chưa có
            if (request.getTransactionCode() == null) {
                request.setTransactionCode("TXN_" + UUID.randomUUID().toString().substring(0, 8));
            }
            
            // Lấy user ID từ JWT token
            Long userId = null;
            try {
                String authHeader = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest().getHeader("Authorization");
                System.out.println("🔍 Auth header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.replace("Bearer ", "");
                    String userIdStr = jwtUtil.getClaimsFromToken(token).getClaims().get("id").toString();
                    userId = Long.parseLong(userIdStr);
                    System.out.println("✅ Found user ID from JWT: " + userId);
                } else {
                    System.out.println("❌ No valid Authorization header found");
                }
            } catch (Exception e) {
                System.out.println("❌ Error getting user ID from JWT: " + e.getMessage());
            }
            
            // Lưu transaction vào database với user ID
            transactionService.createTransaction(
                request.getTransactionCode(),
                (double) request.getAmount(),
                request.getDescription(),
                1L, // pricing ID = 1 (1_MONTH)
                userId // user ID từ JWT
            );
            
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
            
            // Gọi API VietQR chính thức
            String apiUrl;
            if (templateId != null && !templateId.trim().isEmpty() && !templateId.contains("#")) {
                // Sử dụng template ID nếu có và hợp lệ
                apiUrl = String.format("%s/image/970422-%s-%s.jpg?amount=%d&content=%s",
                    vietqrApiUrl,
                    defaultAccountNo,
                    templateId.trim(),
                    request.getAmount(),
                    java.net.URLEncoder.encode(request.getDescription(), "UTF-8")
                );
            } else {
                // Sử dụng API trực tiếp với thông tin ngân hàng
                // Format: https://api.vietqr.io/image/{bankId}?accountNo={accountNo}&amount={amount}&content={content}
                apiUrl = String.format("%s/image/%s?accountNo=%s&amount=%d&content=%s",
                    vietqrApiUrl,
                    request.getBankId(),
                    request.getAccountNo(),
                    request.getAmount(),
                    java.net.URLEncoder.encode(request.getDescription(), "UTF-8")
                );
            }
            
            // Debug log
            System.out.println("Calling VietQR API: " + apiUrl);
            
            try {
                // Thử gọi API VietQR - chỉ kiểm tra xem URL có hợp lệ không
                // Không đọc response content vì nó là binary image data
                restTemplate.headForHeaders(apiUrl);
                System.out.println("VietQR API URL is valid: " + apiUrl);
                
                // Nếu không có exception, URL hợp lệ và có thể sử dụng
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
            } catch (Exception e) {
                System.out.println("VietQR API failed, using local QR: " + e.getMessage());
            }
            
            // Fallback: Tạo QR code locally với format đơn giản
            String vietqrContent = generateVietQRContent(request);
            
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
        try {
            System.out.println("🔍 Checking payment status for: " + transactionCode);
            
            // Kiểm tra trong database trước
            if (transactionService.isTransactionPaid(transactionCode)) {
                System.out.println("✅ Transaction already PAID in database: " + transactionCode);
                return true;
            }
            
            // Lấy thông tin transaction từ database
            var transactionOpt = transactionService.findByCode(transactionCode);
            if (transactionOpt.isPresent()) {
                var transaction = transactionOpt.get();
                double expectedAmount = transaction.getAmount();
                
                System.out.println("🔍 Transaction found in DB: " + transactionCode + ", Amount: " + expectedAmount + ", Status: " + transaction.getPaymentStatus());
                
                // Sử dụng Casso API để check payment status
                boolean isPaid = cassoService.checkPaymentStatus(transactionCode, expectedAmount);
                
                System.out.println("🔍 Casso API result: " + isPaid);
                
                // Nếu thanh toán thành công, cập nhật database
                if (isPaid) {
                    System.out.println("✅ Payment confirmed, updating database...");
                    transactionService.updatePaymentStatus(
                        transactionCode, 
                        Transaction.PaymentStatus.PAID
                    );
                } else {
                    System.out.println("❌ Payment not confirmed by Casso API");
                }
                
                return isPaid;
            } else {
                System.out.println("❌ Transaction not found in database: " + transactionCode);
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Error checking payment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
        // Thử format: bankId:accountNo:amount:content
        return String.format("%s:%s:%d:%s",
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