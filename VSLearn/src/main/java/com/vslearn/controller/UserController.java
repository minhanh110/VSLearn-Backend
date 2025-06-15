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

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;


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
