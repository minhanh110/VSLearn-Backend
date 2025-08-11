package com.vslearn.controller;

import com.vslearn.dto.request.UpdateProfileDTO;
import com.vslearn.dto.request.UserDataDTO;
import com.vslearn.dto.request.UserLoginDTO;
import com.vslearn.dto.request.ChangePasswordDTO;
import com.vslearn.dto.request.ForgotPasswordDTO;
import com.vslearn.dto.request.ResetPasswordDTO;
import com.vslearn.dto.request.VerifySignupOtpDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/users", "/api/v1/users"})
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signin")
    public ResponseEntity<ResponseData<?>> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return userService.signin(userLoginDTO);
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseData<?>> signup(@RequestBody @Valid UserDataDTO userDataDTO) {
        return userService.signup(userDataDTO);
    }

    @PostMapping("/signup/request-otp")
    public ResponseEntity<ResponseData<?>> requestSignupOtp(@RequestParam String email) {
        return userService.requestSignupOtp(email);
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<ResponseData<?>> verifySignupOtp(@RequestBody @Valid VerifySignupOtpDTO dto) {
        return userService.verifySignupOtp(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseData<?>> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody @Valid UpdateProfileDTO dto) {
        return userService.updateProfile(authHeader, dto);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseData<?>> getProfile(@RequestHeader("Authorization") String authHeader) {
        return userService.getProfile(authHeader);
    }

    @GetMapping("/subscription-status")
    public ResponseEntity<ResponseData<?>> getSubscriptionStatus(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return userService.getSubscriptionStatus(authHeader);
    }


    @PutMapping("/change-password")
    public ResponseEntity<ResponseData<?>> changePassword(@RequestHeader("Authorization") String authHeader, @RequestBody @Valid ChangePasswordDTO dto) {
        return userService.changePassword(authHeader, dto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseData<?>> requestPasswordReset(@RequestBody @Valid ForgotPasswordDTO dto) {
        return userService.requestPasswordReset(dto);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseData<?>> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        return userService.verifyOtpAndResetPassword(dto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseData<?>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        throw new UnsupportedOperationException("Not implemented. Nếu cần, hãy chuyển logic sang service.");
    }

    @GetMapping("/content-creators")
    public ResponseEntity<ResponseData<?>> getContentCreators() {
        try {
            List<Map<String, Object>> contentCreators = userService.getContentCreators();
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Lấy danh sách content creators thành công")
                    .data(contentCreators)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Lỗi khi lấy danh sách content creators: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/content-approvers")
    public ResponseEntity<ResponseData<?>> getContentApprovers() {
        try {
            List<Map<String, Object>> contentApprovers = userService.getContentApprovers();
            System.out.println("API: Found " + contentApprovers.size() + " content approvers: " + contentApprovers);
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Lấy danh sách content approvers thành công")
                    .data(contentApprovers)
                    .build());
        } catch (Exception e) {
            System.err.println("API: Error getting content approvers: " + e.getMessage());
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Lỗi khi lấy danh sách content approvers: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/debug/all-users")
    public ResponseEntity<ResponseData<?>> getAllUsersDebug() {
        try {
            // This is a debug endpoint to check all users and their roles
            List<Map<String, Object>> allUsers = userService.getAllUsersDebug();
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Debug: Lấy danh sách tất cả users thành công")
                    .data(allUsers)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Debug: Lỗi khi lấy danh sách users: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/debug/content-approvers")
    public ResponseEntity<ResponseData<?>> getContentApproversDebug() {
        try {
            // This is a debug endpoint to check content approvers
            List<Map<String, Object>> contentApprovers = userService.getContentApprovers();
            return ResponseEntity.ok(ResponseData.builder()
                    .status(200)
                    .message("Debug: Lấy danh sách content approvers thành công")
                    .data(contentApprovers)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseData.builder()
                    .status(500)
                    .message("Debug: Lỗi khi lấy danh sách content approvers: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }
}
