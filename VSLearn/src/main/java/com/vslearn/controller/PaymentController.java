package com.vslearn.controller;

import com.vslearn.dto.request.VietQRRequest;
import com.vslearn.dto.response.VietQRResponse;
import com.vslearn.service.VietQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class PaymentController {
    
    private final VietQRService vietQRService;
    
    @Autowired
    public PaymentController(VietQRService vietQRService) {
        this.vietQRService = vietQRService;
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
            boolean isPaid = vietQRService.checkPaymentStatus(transactionCode);
            
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
} 