package com.vslearn.service;

import com.vslearn.dto.request.UpdateProfileDTO;
import com.vslearn.dto.request.UserDataDTO;
import com.vslearn.dto.request.UserLoginDTO;
import com.vslearn.dto.request.ChangePasswordDTO;
import com.vslearn.dto.request.ForgotPasswordDTO;
import com.vslearn.dto.request.ResetPasswordDTO;
import com.vslearn.dto.request.VerifySignupOtpDTO;
import com.vslearn.dto.response.ResponseData;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<ResponseData<?>> signin(UserLoginDTO userLoginDTO);
    ResponseEntity<ResponseData<?>> signup(UserDataDTO dto);
    ResponseEntity<ResponseData<?>> requestSignupOtp(String email);
    ResponseEntity<ResponseData<?>> verifySignupOtp(VerifySignupOtpDTO dto);
    ResponseEntity<ResponseData<?>> updateProfile(String  authHeader, UpdateProfileDTO dto);
    ResponseEntity<ResponseData<?>> getProfile(String authHeader);
    ResponseEntity<ResponseData<?>> changePassword(String authHeader, ChangePasswordDTO dto);
    ResponseEntity<ResponseData<?>> requestPasswordReset(ForgotPasswordDTO dto);
    ResponseEntity<ResponseData<?>> verifyOtpAndResetPassword(ResetPasswordDTO dto);
    ResponseEntity<ResponseData<?>> handleOAuth2Login(String email, String name);
    ResponseEntity<ResponseData<?>> getSubscriptionStatus(String authHeader);
}
