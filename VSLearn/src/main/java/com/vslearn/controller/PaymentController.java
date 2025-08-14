package com.vslearn.controller;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;
import com.vslearn.service.VietQRService;
import com.vslearn.service.CassoService;
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
    private final JwtUtil jwtUtil;
    
    @Autowired
    public PaymentController(VietQRService vietQRService, CassoService cassoService, 
                           com.vslearn.repository.TransactionRepository transactionRepository,
                           JwtUtil jwtUtil) {
        this.vietQRService = vietQRService;
        this.cassoService = cassoService;
        this.transactionRepository = transactionRepository;
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
            @PathVariable String transactionCode) {
        try {
            System.out.println("🔍 Checking transaction status for: " + transactionCode);
            
            // Tìm transaction trong database
            var transactionOpt = transactionRepository.findByCode(transactionCode);
            if (transactionOpt.isPresent()) {
                var transaction = transactionOpt.get();
                
                System.out.println("🔍 Transaction found: " + transaction.getCode() + 
                                 ", Status: " + transaction.getPaymentStatus() + 
                                 ", Amount: " + transaction.getAmount());
                
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
} 