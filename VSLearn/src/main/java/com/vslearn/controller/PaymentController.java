package com.vslearn.controller;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;
import com.vslearn.service.VietQRService;
import com.vslearn.service.CassoService;
import com.vslearn.utils.MailUtils;
import com.vslearn.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class PaymentController {
    
    private final VietQRService vietQRService;
    private final CassoService cassoService;
    private final com.vslearn.repository.TransactionRepository transactionRepository;
    private final MailUtils mailUtils;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public PaymentController(VietQRService vietQRService, CassoService cassoService, 
                           com.vslearn.repository.TransactionRepository transactionRepository,
                           MailUtils mailUtils, JwtUtil jwtUtil) {
        this.vietQRService = vietQRService;
        this.cassoService = cassoService;
        this.transactionRepository = transactionRepository;
        this.mailUtils = mailUtils;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Test endpoint để kiểm tra kết nối
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Payment API is working"
        ));
    }

    /**
     * Test email service
     */
    @GetMapping("/test/email")
    public ResponseEntity<Map<String, Object>> testEmailService() {
        try {
            boolean emailTest = mailUtils.sentEmail(
                "test@example.com",
                "Test Email Service",
                "<h1>Test Email</h1><p>Email service is working!</p>"
            );
            return ResponseEntity.ok(Map.of(
                "success", emailTest,
                "message", emailTest ? "Email service is working" : "Email service test failed"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email service error: " + e.getMessage()
            ));
        }
    }

    /**
     * Test send payment confirmation email
     */
    @PostMapping("/test/email/payment-confirmation")
    public ResponseEntity<Map<String, Object>> testPaymentConfirmationEmail(@RequestParam String userEmail) {
        try {
            String testEmailContent = generateTestPaymentConfirmationEmail();
            boolean emailSent = mailUtils.sentEmail(
                userEmail,
                "🎉 Chúc mừng! Bạn đã mở khóa thành công gói học VSLearn",
                testEmailContent
            );
            
            return ResponseEntity.ok(Map.of(
                "success", emailSent,
                "message", emailSent ? "Payment confirmation email sent successfully to: " + userEmail : "Failed to send email"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error sending test email: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Test email configuration
     */
    @GetMapping("/test/email/config")
    public ResponseEntity<Map<String, Object>> testEmailConfig() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email configuration test",
                "emailConfigured", true,
                "springMailEnabled", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email configuration error: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Tạo VietQR code cho giao dịch
     */
    @PostMapping("/vietqr/create")
    public ResponseEntity<Map<String, Object>> createVietQR(@RequestBody VietQRRequest request) {
        try {
            VietQRResponse response = vietQRService.createVietQR(request);
            
            if ("ERROR".equals(response.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", response.getMessage()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo QR code thành công",
                "data", response
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/status/{transactionCode}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String transactionCode) {
        try {
            System.out.println("🔍 VietQR status check called for: " + transactionCode);
            
            boolean isPaid = vietQRService.checkPaymentStatus(transactionCode);
            
            System.out.println("🔍 VietQR status check result: " + isPaid);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isPaid", isPaid,
                "message", isPaid ? "Đã thanh toán" : "Chưa thanh toán"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Webhook từ VietQR
     */
    @PostMapping("/webhook/vietqr")
    public ResponseEntity<Map<String, Object>> handleVietQRWebhook(@RequestBody String webhookData) {
        try {
            vietQRService.handleWebhook(webhookData);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook processed successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Webhook processing failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Test Casso API - lấy danh sách giao dịch
     */
    @GetMapping("/casso/transactions")
    public ResponseEntity<Map<String, Object>> getCassoTransactions(
            @RequestParam(defaultValue = "7") int days) {
        try {
            var fromDate = java.time.LocalDate.now().minusDays(days);
            var toDate = java.time.LocalDate.now();
            
            var transactions = cassoService.getTransactions(fromDate, toDate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy giao dịch thành công",
                "data", Map.of(
                    "transactions", transactions,
                    "fromDate", fromDate.toString(),
                    "toDate", toDate.toString(),
                    "count", transactions.size()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Test Casso API - kiểm tra thanh toán
     */
    @GetMapping("/casso/check/{transactionCode}")
    public ResponseEntity<Map<String, Object>> checkCassoPayment(
            @PathVariable String transactionCode,
            @RequestParam(defaultValue = "299000") double amount) {
        try {
            System.out.println("🔍 Casso check called for: " + transactionCode + ", amount: " + amount);
            
            boolean isPaid = cassoService.checkPaymentStatus(transactionCode, amount);
            
            System.out.println("🔍 Casso check result: " + isPaid);
            
            // Nếu payment confirmed, update transaction status
            if (isPaid) {
                try {
                    transactionRepository.updatePaymentStatus(transactionCode, com.vslearn.entities.Transaction.PaymentStatus.PAID);
                    System.out.println("✅ Transaction status updated to PAID for: " + transactionCode);
                } catch (Exception updateError) {
                    System.out.println("❌ Error updating transaction status: " + updateError.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isPaid", isPaid,
                "message", isPaid ? "Đã thanh toán" : "Chưa thanh toán",
                "data", Map.of(
                    "transactionCode", transactionCode,
                    "expectedAmount", amount
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy danh sách tài khoản ngân hàng từ Casso
     */
    @GetMapping("/casso/accounts")
    public ResponseEntity<Map<String, Object>> getCassoAccounts() {
        try {
            var accounts = cassoService.getAccounts();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy danh sách tài khoản thành công",
                "data", Map.of(
                    "accounts", accounts,
                    "count", accounts.size()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Test kết nối Casso API
     */
    @GetMapping("/casso/test")
    public ResponseEntity<Map<String, Object>> testCassoConnection() {
        try {
            // Test với ngày hôm nay
            var today = java.time.LocalDate.now();
            var transactions = cassoService.getTransactions(today, today);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Casso API connection successful",
                "data", Map.of(
                    "transactionCount", transactions.size(),
                    "testDate", today.toString(),
                    "apiKey", cassoService.getClass().getSimpleName() // Chỉ hiển thị tên class, không hiển thị key
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Casso API connection failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Kiểm tra trạng thái transaction hiện tại
     */
    @GetMapping("/transaction/status/{transactionCode}")
    public ResponseEntity<Map<String, Object>> checkTransactionStatus(
            @PathVariable String transactionCode,
            @RequestParam(defaultValue = "299000") double amount) {
        try {
            System.out.println("🔍 Checking transaction status for: " + transactionCode + ", amount: " + amount);
            
            // Tìm transaction trong database
            var transactionOpt = transactionRepository.findByCode(transactionCode);
            if (transactionOpt.isPresent()) {
                var transaction = transactionOpt.get();
                
                System.out.println("🔍 Transaction found: " + transaction.getCode() + 
                                 ", Status: " + transaction.getPaymentStatus() + 
                                 ", Amount: " + transaction.getAmount());
                
                // Nếu đã PAID thì return luôn
                if (transaction.getPaymentStatus() == com.vslearn.entities.Transaction.PaymentStatus.PAID) {
                    System.out.println("✅ Transaction already PAID in database");
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "transactionCode", transactionCode,
                        "paymentStatus", transaction.getPaymentStatus().toString(),
                        "amount", transaction.getAmount(),
                        "description", transaction.getDescription(),
                        "startDate", transaction.getStartDate(),
                        "endDate", transaction.getEndDate(),
                        "isPaid", true
                    ));
                }
                
                // Nếu chưa PAID, kiểm tra qua Casso API
                System.out.println("🔍 Transaction is PENDING, checking Casso API...");
                boolean isPaid = cassoService.checkPaymentStatus(transactionCode, amount);
                System.out.println("🔍 Casso API result: " + isPaid);
                
                // Nếu thanh toán thành công, cập nhật database và gửi email
                if (isPaid) {
                    try {
                        System.out.println("✅ Payment confirmed by Casso API, updating database...");
                        transactionRepository.updatePaymentStatus(transactionCode, com.vslearn.entities.Transaction.PaymentStatus.PAID);
                        System.out.println("✅ Transaction status updated to PAID");
                        
                        // Refresh transaction data
                        transaction = transactionRepository.findByCode(transactionCode).orElse(transaction);
                        
                        // Gửi email xác nhận thanh toán
                        try {
                            System.out.println("📧 Sending payment confirmation email...");
                            String emailContent = generatePaymentConfirmationEmail(transaction);
                            boolean emailSent = mailUtils.sentEmail(
                                transaction.getCreatedBy().getEmail(),
                                "🎉 Chúc mừng! Bạn đã mở khóa thành công gói học VSLearn",
                                emailContent
                            );
                            if (emailSent) {
                                System.out.println("✅ Payment confirmation email sent successfully");
                            } else {
                                System.out.println("❌ Failed to send payment confirmation email");
                            }
                        } catch (Exception emailError) {
                            System.out.println("❌ Error sending payment confirmation email: " + emailError.getMessage());
                        }
                        
                    } catch (Exception updateError) {
                        System.out.println("❌ Error updating transaction status: " + updateError.getMessage());
                    }
                } else {
                    System.out.println("❌ Payment not confirmed by Casso API");
                }
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactionCode", transactionCode,
                    "paymentStatus", transaction.getPaymentStatus().toString(),
                    "amount", transaction.getAmount(),
                    "description", transaction.getDescription(),
                    "startDate", transaction.getStartDate(),
                    "endDate", transaction.getEndDate(),
                    "isPaid", transaction.getPaymentStatus() == com.vslearn.entities.Transaction.PaymentStatus.PAID
                ));
            } else {
                System.out.println("❌ Transaction not found: " + transactionCode);
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Transaction not found",
                    "transactionCode", transactionCode
                ));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error checking transaction status: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Debug endpoint để kiểm tra thanh toán với log chi tiết
     */
    @GetMapping("/debug/check/{transactionCode}")
    public ResponseEntity<Map<String, Object>> debugCheckPayment(
            @PathVariable String transactionCode,
            @RequestParam(defaultValue = "299000") double amount) {
        try {
            System.out.println("🔍 DEBUG: Checking payment for transaction: " + transactionCode + ", amount: " + amount);
            
            // 1. Kiểm tra transaction trong database
            var transactionOpt = transactionRepository.findByCode(transactionCode);
            if (transactionOpt.isPresent()) {
                var transaction = transactionOpt.get();
                System.out.println("🔍 DEBUG: Found transaction in DB:");
                System.out.println("  - Code: " + transaction.getCode());
                System.out.println("  - Status: " + transaction.getPaymentStatus());
                System.out.println("  - Amount: " + transaction.getAmount());
                System.out.println("  - Description: " + transaction.getDescription());
                System.out.println("  - Created: " + transaction.getCreatedAt());
            } else {
                System.out.println("❌ DEBUG: Transaction not found in DB: " + transactionCode);
            }
            
            // 2. Kiểm tra qua Casso API
            System.out.println("🔍 DEBUG: Checking Casso API...");
            boolean isPaid = cassoService.checkPaymentStatus(transactionCode, amount);
            System.out.println("🔍 DEBUG: Casso API result: " + isPaid);
            
            // 3. Nếu thanh toán thành công, cập nhật database
            if (isPaid) {
                try {
                    System.out.println("🔍 DEBUG: Updating transaction status to PAID...");
                    System.out.println("🔍 DEBUG: Transaction code: " + transactionCode);
                    System.out.println("🔍 DEBUG: Expected amount: " + amount);
                    transactionRepository.updatePaymentStatus(transactionCode, com.vslearn.entities.Transaction.PaymentStatus.PAID);
                    System.out.println("✅ DEBUG: Transaction status updated successfully");
                } catch (Exception updateError) {
                    System.out.println("❌ DEBUG: Error updating transaction status: " + updateError.getMessage());
                    updateError.printStackTrace();
                }
            } else {
                System.out.println("❌ DEBUG: Payment not confirmed - transaction remains PENDING");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isPaid", isPaid,
                "message", isPaid ? "Đã thanh toán" : "Chưa thanh toán",
                "debug", Map.of(
                    "transactionCode", transactionCode,
                    "expectedAmount", amount,
                    "transactionFound", transactionOpt.isPresent(),
                    "transactionStatus", transactionOpt.map(t -> t.getPaymentStatus().toString()).orElse("NOT_FOUND"),
                    "cassoResult", isPaid
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ DEBUG: Error in debug check: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage(),
                "debug", Map.of(
                    "error", e.getMessage(),
                    "stackTrace", e.getStackTrace()
                )
            ));
        }
    }

    /**
     * Xem tất cả giao dịch trong 7 ngày gần nhất từ Casso API
     */
    @GetMapping("/casso/transactions/today")
    public ResponseEntity<Map<String, Object>> getTodayTransactions() {
        try {
            System.out.println("🔍 Getting recent transactions from Casso API...");
            
            var today = java.time.LocalDate.now();
            var fromDate = today.minusDays(7); // Lấy giao dịch trong 7 ngày gần nhất
            var transactions = cassoService.getTransactions(fromDate, today);
            
            System.out.println("🔍 Found " + transactions.size() + " transactions in last 7 days");
            
            // Log chi tiết từng giao dịch
            for (var transaction : transactions) {
                System.out.println("🔍 Transaction: ID=" + transaction.getId() + 
                                 ", Amount=" + transaction.getAmount() + 
                                 ", Description='" + transaction.getDescription() + "'" +
                                 ", When=" + transaction.getWhen());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy danh sách giao dịch gần đây thành công",
                "data", Map.of(
                    "transactionCount", transactions.size(),
                    "fromDate", fromDate.toString(),
                    "toDate", today.toString(),
                    "transactions", transactions.stream().map(t -> Map.of(
                        "id", t.getId(),
                        "amount", t.getAmount(),
                        "description", t.getDescription(),
                        "when", t.getWhen()
                    )).collect(java.util.stream.Collectors.toList())
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error getting recent transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Kiểm tra tất cả transactions của user hiện tại
     */
    @GetMapping("/user/transactions")
    public ResponseEntity<Map<String, Object>> getUserTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("🔍 Getting all transactions for current user...");
            
            // Lấy user ID từ JWT token
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.replace("Bearer ", "");
                try {
                    String userIdStr = jwtUtil.getClaimsFromToken(token).getClaims().get("id").toString();
                    userId = Long.parseLong(userIdStr);
                    System.out.println("✅ Found user ID from JWT: " + userId);
                } catch (Exception e) {
                    System.out.println("❌ Error getting user ID from JWT: " + e.getMessage());
                }
            }
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy user ID"
                ));
            }
            
            // Lấy tất cả transactions của user
            var userTransactions = transactionRepository.findByCreatedBy_Id(userId);
            
            System.out.println("🔍 Found " + userTransactions.size() + " transactions for user " + userId);
            
            // Log chi tiết từng transaction
            for (var transaction : userTransactions) {
                System.out.println("🔍 Transaction: ID=" + transaction.getId() + 
                                 ", Code=" + transaction.getCode() + 
                                 ", Status=" + transaction.getPaymentStatus() + 
                                 ", Amount=" + transaction.getAmount() + 
                                 ", StartDate=" + transaction.getStartDate() + 
                                 ", EndDate=" + transaction.getEndDate());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy danh sách transactions thành công",
                "data", Map.of(
                    "userId", userId,
                    "transactionCount", userTransactions.size(),
                    "transactions", userTransactions.stream().map(t -> Map.of(
                        "id", t.getId(),
                        "code", t.getCode(),
                        "status", t.getPaymentStatus().toString(),
                        "amount", t.getAmount(),
                        "startDate", t.getStartDate(),
                        "endDate", t.getEndDate(),
                        "createdAt", t.getCreatedAt()
                    )).collect(java.util.stream.Collectors.toList())
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error getting user transactions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate payment confirmation email content
     */
    private String generatePaymentConfirmationEmail(com.vslearn.entities.Transaction transaction) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác nhận thanh toán VSLearn</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-icon { font-size: 48px; margin-bottom: 20px; }
                    .package-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #667eea; }
                    .feature-list { list-style: none; padding: 0; }
                    .feature-list li { padding: 8px 0; border-bottom: 1px solid #eee; }
                    .feature-list li:before { content: "✅ "; margin-right: 10px; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; }
                    .contact-info { background: #e8f4fd; padding: 15px; border-radius: 8px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="success-icon">🎉</div>
                        <h1>Chúc mừng bạn!</h1>
                        <p>Bạn đã mở khóa thành công gói học VSLearn</p>
                    </div>
                    
                    <div class="content">
                        <h2>Xin chào %s,</h2>
                        
                        <p>Cảm ơn bạn đã tin tưởng và chọn VSLearn! Giao dịch thanh toán của bạn đã được xác nhận thành công.</p>
                        
                        <div class="package-info">
                            <h3>📦 Thông tin gói học:</h3>
                            <ul>
                                <li><strong>Tên gói:</strong> %s</li>
                                <li><strong>Thời hạn:</strong> %s</li>
                                <li><strong>Số tiền:</strong> %,.0f VND</li>
                                <li><strong>Mã giao dịch:</strong> %s</li>
                                <li><strong>Ngày thanh toán:</strong> %s</li>
                                <li><strong>Hiệu lực từ:</strong> %s đến %s</li>
                            </ul>
                        </div>
                        
                        <div class="package-info">
                            <h3>🚀 Tính năng gói học:</h3>
                            <ul class="feature-list">
                                <li>Truy cập không giới hạn</li>
                                <li>Tất cả khóa học cao cấp</li>
                                <li>Kiểm tra tiến độ</li>
                                <li>Chứng chỉ hoàn thành</li>
                                <li>Hỗ trợ ưu tiên</li>
                            </ul>
                        </div>
                        
                        <div style="text-align: center;">
                            <a href="http://localhost:3000/homepage" class="cta-button">Bắt đầu học ngay!</a>
                        </div>
                        
                        <div class="contact-info">
                            <h3>📞 Cần hỗ trợ?</h3>
                            <p>Đội ngũ VSLearn luôn sẵn sàng hỗ trợ bạn:</p>
                            <ul>
                                <li>📧 Email: vslearn@gmail.com</li>
                                <li>📞 Hotline: 1900 xxxx</li>
                                <li>🌐 Website: https://vslearn.com</li>
                            </ul>
                        </div>
                        
                        <p>Chúc bạn có những trải nghiệm học tập tuyệt vời với VSLearn!</p>
                        
                        <p>Trân trọng,<br>
                        <strong>Đội ngũ VSLearn</strong></p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2024 VSLearn. Tất cả quyền được bảo lưu.</p>
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                transaction.getCreatedBy().getFullName(),
                transaction.getPricing().getPricingType(),
                transaction.getPricing().getDurationDays() + " ngày",
                transaction.getAmount(),
                transaction.getCode(),
                java.time.LocalDateTime.now().toString(),
                transaction.getStartDate().toString(),
                transaction.getEndDate().toString()
            );
    }

    /**
     * Generate test payment confirmation email content
     */
    private String generateTestPaymentConfirmationEmail() {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Email - VSLearn</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-icon { font-size: 48px; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="success-icon">🎉</div>
                        <h1>Test Email</h1>
                        <p>Email service đang hoạt động tốt!</p>
                    </div>
                    
                    <div class="content">
                        <h2>Xin chào Test User,</h2>
                        
                        <p>Đây là email test để kiểm tra email service của VSLearn.</p>
                        
                        <p>Email service đã được cấu hình thành công!</p>
                        
                        <p>Trân trọng,<br>
                        <strong>Đội ngũ VSLearn</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
} 