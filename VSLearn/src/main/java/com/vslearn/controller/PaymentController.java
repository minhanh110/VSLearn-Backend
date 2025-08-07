package com.vslearn.controller;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;
import com.vslearn.service.VietQRService;
import com.vslearn.service.CassoService;
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
    
    @Autowired
    public PaymentController(VietQRService vietQRService, CassoService cassoService, 
                           com.vslearn.repository.TransactionRepository transactionRepository) {
        this.vietQRService = vietQRService;
        this.cassoService = cassoService;
        this.transactionRepository = transactionRepository;
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
} 