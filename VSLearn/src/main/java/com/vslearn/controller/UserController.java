package com.vslearn.controller;

import com.vslearn.dto.request.UpdateProfileDTO;
import com.vslearn.dto.request.UserDataDTO;
import com.vslearn.dto.request.UserLoginDTO;
import com.vslearn.dto.request.ChangePasswordDTO;
import com.vslearn.dto.request.ForgotPasswordDTO;
import com.vslearn.dto.request.ResetPasswordDTO;
import com.vslearn.dto.request.VerifySignupOtpDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.Transaction;
import com.vslearn.entities.User;
import com.vslearn.repository.TransactionRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.UserService;
import com.vslearn.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return userService.signin(userLoginDTO);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserDataDTO userDataDTO) {
        return userService.signup(userDataDTO);
    }

    @PostMapping("/signup/request-otp")
    public ResponseEntity<?> requestSignupOtp(@RequestParam String email) {
        return userService.requestSignupOtp(email);
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> verifySignupOtp(@RequestBody @Valid VerifySignupOtpDTO dto) {
        return userService.verifySignupOtp(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader
            , @RequestBody @Valid UpdateProfileDTO dto) {
        return userService.updateProfile(authHeader, dto);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        return userService.getProfile(authHeader);
    }

    @GetMapping("/subscription-status")
    public ResponseEntity<?> getSubscriptionStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = (String) jwtUtil.getClaimsFromToken(token).getClaims().get("id");
            
            if (userId == null) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(400)
                        .message("Invalid token")
                        .data(null)
                        .build());
            }

            User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(404)
                        .message("User not found")
                        .data(null)
                        .build());
            }

            // Kiểm tra subscription
            List<Transaction> userTransactions = transactionRepository.findByCreatedBy_Id(user.getId());
            
            if (userTransactions.isEmpty()) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No subscription found")
                        .data(Map.of(
                            "hasSubscription", false,
                            "userType", "registered",
                            "maxTopics", 2
                        ))
                        .build());
            }

            // Sắp xếp theo thời gian tạo, lấy transaction mới nhất
            Transaction latestTransaction = userTransactions.stream()
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .findFirst()
                    .orElse(null);

            if (latestTransaction == null) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No valid subscription found")
                        .data(Map.of(
                            "hasSubscription", false,
                            "userType", "registered",
                            "maxTopics", 2
                        ))
                        .build());
            }

            // Kiểm tra xem gói học có còn hiệu lực không
            Instant now = Instant.now();
            boolean isValid = now.isAfter(latestTransaction.getStartDate()) && 
                             now.isBefore(latestTransaction.getEndDate());

            if (isValid) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("Valid subscription found")
                        .data(Map.of(
                            "hasSubscription", true,
                            "userType", "premium",
                            "maxTopics", Integer.MAX_VALUE,
                            "subscriptionEndDate", latestTransaction.getEndDate(),
                            "pricingType", latestTransaction.getPricing().getPricingType()
                        ))
                        .build());
            } else {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("Subscription expired")
                        .data(Map.of(
                            "hasSubscription", false,
                            "userType", "registered",
                            "maxTopics", 2,
                            "expiredDate", latestTransaction.getEndDate()
                        ))
                        .build());
            }

        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Internal server error: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody @Valid ChangePasswordDTO dto) {
        return userService.changePassword(authHeader, dto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody @Valid ForgotPasswordDTO dto) {
        return userService.requestPasswordReset(dto);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        return userService.verifyOtpAndResetPassword(dto);
    }
}
