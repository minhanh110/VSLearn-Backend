package com.vslearn.service.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.vslearn.dto.request.UserDataDTO;
import com.vslearn.dto.request.UserLoginDTO;
import com.vslearn.dto.request.UpdateProfileDTO;
import com.vslearn.dto.request.ChangePasswordDTO;
import com.vslearn.dto.request.ForgotPasswordDTO;
import com.vslearn.dto.request.ResetPasswordDTO;
import com.vslearn.dto.request.VerifySignupOtpDTO;
import com.vslearn.dto.response.ProfileDTO;
import com.vslearn.dto.response.ResponseData;
import com.vslearn.entities.User;
import com.vslearn.exception.customizeException.AddingFailException;
import com.vslearn.exception.customizeException.AuthenticationFailException;
import com.vslearn.exception.customizeException.ResourceNotFoundException;
import com.vslearn.repository.UserRepository;
import com.vslearn.service.UserService;
import com.vslearn.utils.JwtUtil;
import com.vslearn.utils.MailUtils;
import com.vslearn.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RandomUtils randomUtils;
    @Autowired
    private MailUtils mailUtils;

    @Override
    public ResponseEntity<?> signin(UserLoginDTO userLoginDTO) {
        User user = userRepository.findByUserName(userLoginDTO.getUsername())
                .orElseThrow(() -> new AuthenticationFailException("Tài khoản hoặc mật khẩu không đúng", userLoginDTO));
        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getUserPassword())) {
            throw new AuthenticationFailException("Tài khoản hoặc mật khẩu không đúng", userLoginDTO);
        }
        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công")
                .data(jwtUtil.generateToken(user.getId()+"", user.getUserEmail(), user.getUserRole()))
                .build());
    }

    @Override
    public ResponseEntity<?> signup(UserDataDTO dto) {
        // Check if username exists
        if (userRepository.existsByUserName(dto.getUsername())) {
            throw new AddingFailException("Tên đăng nhập đã tồn tại", dto);
        }

        // Check if email exists and is verified
        User user = userRepository.findByUserEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email chưa được xác thực", dto.getEmail()));

        if (!user.getIsActive()) {
            throw new AuthenticationFailException("Email chưa được xác thực", dto);
        }

        // Check if phone number is already registered
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isEmpty()) {
            Optional<User> existingUserWithPhone = userRepository.findByPhoneNumber(dto.getPhoneNumber());
            if (existingUserWithPhone.isPresent()) {
                throw new AddingFailException("Số điện thoại đã được đăng ký", dto);
            }
        }

        // Update user with registration data
        user.setUserName(dto.getUsername());
        user.setFirstName(dto.getFirstName() != null ? dto.getFirstName() : user.getFirstName());
        user.setLastName(dto.getLastName() != null ? dto.getLastName() : user.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setUserPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserRole(dto.getUserRole() != null ? dto.getUserRole() : "ROLE_LEARNER");
        user.setActiveCode(null); // Clear OTP after successful registration
        user.setIsActive(true); // Ensure account is active after registration

        userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Đăng ký thành công")
                .data(jwtUtil.generateToken(user.getId()+"", user.getUserEmail(), user.getUserRole()))
                .build());
    }

    @Override
    public ResponseEntity<?> requestSignupOtp(String email) {
        // Check if email is already registered and active
        Optional<User> existingUser = userRepository.findByUserEmail(email);
        if (existingUser.isPresent() && existingUser.get().getIsActive()) {
            throw new AddingFailException("Email đã được đăng ký", email);
        }

        String otp = randomUtils.getRandomActiveCodeNumber(6L);
        String encodedOtp = passwordEncoder.encode(otp);
        
        User user;
        
        if (existingUser.isPresent()) {
            // Update existing user with new OTP
            user = existingUser.get();
            user.setActiveCode(otp);
            user.setUserPassword(encodedOtp);
            user.setModifyTime(Instant.now().plusSeconds(300)); // OTP valid for 5 minutes
            user.setIsActive(false); // Ensure account is not active until OTP verification
        } else {
            // Create new temporary user
            user = User.builder()
                    .userEmail(email)
                    .userName(email.split("@")[0])
                    .firstName("Temporary")
                    .lastName("User")
                    .userRole("ROLE_LEARNER")
                    .userPassword(encodedOtp)
                    .isActive(false)
                    .activeCode(otp)
                    .createdAt(Instant.now())
                    .modifyTime(Instant.now().plusSeconds(300))
                    .build();
        }

        // Save user
        userRepository.save(user);

        if(!mailUtils.sentEmail(email, "Mã xác thực đăng ký tài khoản VSLearn", 
            "Mã xác thực của bạn là: " + otp + "\nMã này có hiệu lực trong 5 phút.")) {
            throw new AuthenticationFailException("Không thể gửi mã xác thực", email);
        }

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Mã xác thực đã được gửi đến email của bạn")
                .build());
    }

    @Override
    public ResponseEntity<?> verifySignupOtp(VerifySignupOtpDTO dto) {
        User user = userRepository.findByUserEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email chưa được đăng ký", dto.getEmail()));

        // Check if OTP matches
        if (!user.getActiveCode().equals(dto.getOtp())) {
            throw new AuthenticationFailException("Mã xác thực không đúng", dto);
        }

        // Check if OTP has expired
        if (user.getModifyTime().isBefore(Instant.now())) {
            throw new AuthenticationFailException("Mã xác thực đã hết hạn", dto);
        }

        // Activate user
        user.setIsActive(true);
        user.setActiveCode(null); // Clear OTP after successful verification
        userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Xác thực email thành công")
                .build());
    }

    @Override
    public ResponseEntity<?> updateProfile(String authHeader, UpdateProfileDTO dto) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        JWTClaimsSet claimsSet = jwtUtil.getClaimsFromToken(token);
        Long userId = Long.parseLong((String)claimsSet.getClaims().get("id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId, dto));

        // Update user information
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setUserAvatar(dto.getUserAvatar());
        user.setUpdatedAt(Instant.now());

        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Profile updated successfully!")
                .data(updatedUser)
                .build());
    }

    @Override
    public ResponseEntity<?> getProfile(String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        JWTClaimsSet claimsSet = jwtUtil.getClaimsFromToken(token);
        Long userId = Long.parseLong((String)claimsSet.getClaims().get("id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        ProfileDTO profileDTO = ProfileDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .userAvatar(user.getUserAvatar())
                .build();
        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Profile get successfully!")
                .data(profileDTO)
                .build());
    }

    @Override
    public ResponseEntity<?> changePassword(String authHeader, ChangePasswordDTO dto) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        JWTClaimsSet claimsSet = jwtUtil.getClaimsFromToken(token);
        Long userId = Long.parseLong((String)claimsSet.getClaims().get("id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getUserPassword())) {
            throw new AuthenticationFailException("Current password is incorrect", dto);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new AddingFailException("New password and confirm password do not match", dto);
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getUserPassword())) {
            throw new AddingFailException("New password must be different from current password", dto);
        }

        user.setUserPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Password changed successfully")
                .build());
    }

    @Override
    public ResponseEntity<?> requestPasswordReset(ForgotPasswordDTO dto) {
        User user = userRepository.findByUserEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + dto.getEmail()));

        String otp = randomUtils.getRandomActiveCodeNumber(6L);

        user.setActiveCode(otp);
        user.setModifyTime(Instant.now().plusSeconds(300)); // OTP valid for 5 minutes
        userRepository.save(user);

        if(!mailUtils.sentEmail(dto.getEmail(), "Your active code: ", otp)){
            throw new AuthenticationFailException("Your active code is incorrect", dto);
        }

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .data(otp)
                .message("OTP has been sent to your email")
                .build());
    }

    @Override
    public ResponseEntity<?> verifyOtpAndResetPassword(ResetPasswordDTO dto) {
        User user = userRepository.findByActiveCode(dto.getOtp())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired OTP"));

        if (user.getModifyTime().isBefore(Instant.now())) {
            user.setModifyTime(null);
            user.setActiveCode(null);
            userRepository.save(user);
            throw new AuthenticationFailException("OTP has expired", dto);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new AuthenticationFailException("New password and confirm password do not match", dto);
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getUserPassword())) {
            throw new AuthenticationFailException("New password must be different from current password", dto);
        }

        user.setUserPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setActiveCode(null);
        user.setModifyTime(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Password has been reset successfully")
                .build());
    }

    @Override
    public ResponseEntity<?> handleOAuth2Login(String email, String name) {
        User user = userRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    // Tạo user mới nếu chưa tồn tại
                    User newUser = User.builder()
                        .userEmail(email)
                        .userName(email.split("@")[0]) // Tạo username từ email
                        .firstName(name)
                        .lastName("")
                        .userRole("ROLE_LEARNER")
                        .isActive(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .provider("GOOGLE")
                        .build();
                    return userRepository.save(newUser);
                });

        // Tạo JWT token và trả về
        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công")
                .data(jwtUtil.generateToken(user.getId()+"", user.getUserEmail(), user.getUserRole()))
                .build());
    }

}
