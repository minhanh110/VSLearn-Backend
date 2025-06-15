package com.vslearn.service;

import com.vslearn.dto.request.UpdateProfileDTO;
import com.vslearn.dto.request.UserDataDTO;
import com.vslearn.dto.request.UserLoginDTO;
import com.vslearn.dto.request.ChangePasswordDTO;
import com.vslearn.dto.request.ForgotPasswordDTO;
import com.vslearn.dto.request.ResetPasswordDTO;
import com.vslearn.dto.request.VerifySignupOtpDTO;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<?> signin(UserLoginDTO userLoginDTO);
    ResponseEntity<?> signup(UserDataDTO dto);
    ResponseEntity<?> requestSignupOtp(String email);
    ResponseEntity<?> verifySignupOtp(VerifySignupOtpDTO dto);
    ResponseEntity<?> updateProfile(String  authHeader, UpdateProfileDTO dto);
    ResponseEntity<?> getProfile(String authHeader);
    ResponseEntity<?> changePassword(String authHeader, ChangePasswordDTO dto);
    ResponseEntity<?> requestPasswordReset(ForgotPasswordDTO dto);
    ResponseEntity<?> verifyOtpAndResetPassword(ResetPasswordDTO dto);
}
