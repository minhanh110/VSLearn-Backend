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
import com.vslearn.repository.TransactionRepository;
import com.vslearn.service.UserService;
import com.vslearn.service.CassoService;
import com.vslearn.utils.JwtUtil;
import com.vslearn.utils.MailUtils;
import com.vslearn.utils.RandomUtils;
import com.vslearn.constant.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
@Component
@Slf4j
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

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CassoService cassoService;

    @Override
    public ResponseEntity<ResponseData<?>> signin(UserLoginDTO userLoginDTO) {
        User user = userRepository.findByUserName(userLoginDTO.getUsername())
                .orElseThrow(() -> new AuthenticationFailException("Tài khoản hoặc mật khẩu không đúng", userLoginDTO));
        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getUserPassword())) {
            throw new AuthenticationFailException("Tài khoản hoặc mật khẩu không đúng", userLoginDTO);
        }
        // Nếu user chưa có role, gán mặc định là LEARNER
        if (user.getUserRole() == null || user.getUserRole().isEmpty()) {
            user.setUserRole(UserRoles.LEARNER);
            userRepository.save(user);
        }
        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công")
                .data(jwtUtil.generateToken(user.getId()+"", user.getUserEmail(), user.getUserRole()))
                .build());
    }

    @Override
    public ResponseEntity<ResponseData<?>> signup(UserDataDTO dto) {
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
        user.setUserRole(UserRoles.LEARNER); // luôn là LEARNER khi đăng ký
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
    public ResponseEntity<ResponseData<?>> requestSignupOtp(String email) {
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
    public ResponseEntity<ResponseData<?>> verifySignupOtp(VerifySignupOtpDTO dto) {
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
    public ResponseEntity<ResponseData<?>> updateProfile(String authHeader, UpdateProfileDTO dto) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        JWTClaimsSet claimsSet = jwtUtil.getClaimsFromToken(token);
        Long userId = Long.parseLong((String)claimsSet.getClaims().get("id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId, dto));

        // Check if phone number is already used by another user
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isEmpty()) {
            Optional<User> existingUserWithPhone = userRepository.findByPhoneNumber(dto.getPhoneNumber());
            if (existingUserWithPhone.isPresent() && !existingUserWithPhone.get().getId().equals(userId)) {
                throw new AddingFailException("Số điện thoại đã được đăng ký bởi tài khoản khác", dto);
            }
        }

        // Update user information
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setUserAvatar(dto.getUserAvatar());
        user.setUpdatedAt(Instant.now());

        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật hồ sơ thành công!")
                .data(updatedUser)
                .build());
    }

    @Override
    public ResponseEntity<ResponseData<?>> getProfile(String authHeader) {
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
                .message("Lấy thông tin hồ sơ thành công!")
                .data(profileDTO)
                .build());
    }

    @Override
    public ResponseEntity<ResponseData<?>> changePassword(String authHeader, ChangePasswordDTO dto) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        JWTClaimsSet claimsSet = jwtUtil.getClaimsFromToken(token);
        Long userId = Long.parseLong((String)claimsSet.getClaims().get("id"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getUserPassword())) {
            throw new AuthenticationFailException("Mật khẩu hiện tại không chính xác", dto);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new AddingFailException("Mật khẩu mới và xác nhận mật khẩu không khớp", dto);
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getUserPassword())) {
            throw new AddingFailException("Mật khẩu mới phải khác mật khẩu hiện tại", dto);
        }

        user.setUserPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Đổi mật khẩu thành công")
                .build());
    }

    @Override
    public ResponseEntity<ResponseData<?>> requestPasswordReset(ForgotPasswordDTO dto) {
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
    public ResponseEntity<ResponseData<?>> verifyOtpAndResetPassword(ResetPasswordDTO dto) {
        // Check if OTP is provided
        if (dto.getOtp() == null || dto.getOtp().trim().isEmpty()) {
            throw new AddingFailException("Mã OTP không được để trống", dto);
        }

        User user = userRepository.findByActiveCode(dto.getOtp())
                .orElseThrow(() -> new ResourceNotFoundException("Mã OTP không hợp lệ hoặc đã hết hạn", dto.getOtp()));

        if (user.getModifyTime().isBefore(Instant.now())) {
            user.setModifyTime(null);
            user.setActiveCode(null);
            userRepository.save(user);
            throw new AuthenticationFailException("Mã OTP đã hết hạn", dto);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new AuthenticationFailException("Mật khẩu mới và xác nhận mật khẩu không khớp", dto);
        }

        // Check if new password is same as current password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getUserPassword())) {
            throw new AuthenticationFailException("Mật khẩu mới phải khác mật khẩu hiện tại", dto);
        }

        user.setUserPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setActiveCode(null);
        user.setModifyTime(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message("Đặt lại mật khẩu thành công")
                .build());
    }

    @Override
    public ResponseEntity<ResponseData<?>> handleOAuth2Login(String email, String name) {
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

    @Override
    public ResponseEntity<ResponseData<?>> getSubscriptionStatus(String authHeader) {
        try {
            // Nếu không có Authorization header, trả về guest user
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("Guest user")
                        .data(java.util.Map.of(
                            "hasSubscription", false,
                            "userType", "guest",
                            "maxTopics", 1
                        ))
                        .build());
            }
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
            // Kiểm tra subscription - lấy tất cả transactions (cả PENDING và PAID)
            java.util.List<com.vslearn.entities.Transaction> userTransactions = transactionRepository.findByCreatedBy_Id(user.getId());
            log.info("Found {} transactions for user ID: {}", userTransactions.size(), user.getId());
            
            // Log tất cả transactions để debug
            for (com.vslearn.entities.Transaction t : userTransactions) {
                log.info("Transaction: ID={}, Status={}, StartDate={}, EndDate={}, Amount={}", 
                        t.getId(), t.getPaymentStatus(), t.getStartDate(), t.getEndDate(), t.getAmount());
            }
            
            if (userTransactions.isEmpty()) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No subscription found")
                        .data(java.util.Map.of(
                            "hasSubscription", false,
                            "userType", "registered",
                            "maxTopics", 2
                        ))
                        .build());
            }
            // Sắp xếp theo thời gian tạo, lấy transaction mới nhất
            com.vslearn.entities.Transaction latestTransaction = userTransactions.stream()
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .findFirst()
                    .orElse(null);
            
            if (latestTransaction != null) {
                log.info("Latest transaction for user {}: ID={}, Status={}, StartDate={}, EndDate={}", 
                        user.getId(), latestTransaction.getId(), latestTransaction.getPaymentStatus(),
                        latestTransaction.getStartDate(), latestTransaction.getEndDate());
                
                // Nếu transaction là PENDING, thử check payment status
                if (latestTransaction.getPaymentStatus() == com.vslearn.entities.Transaction.PaymentStatus.PENDING) {
                    log.info("Checking payment status for pending transaction: {}", latestTransaction.getCode());
                    boolean isPaid = cassoService.checkPaymentStatus(latestTransaction.getCode(), latestTransaction.getAmount());
                    if (isPaid) {
                        log.info("Payment confirmed for transaction: {}", latestTransaction.getCode());
                        // Update transaction status to PAID
                        transactionRepository.updatePaymentStatus(latestTransaction.getCode(), com.vslearn.entities.Transaction.PaymentStatus.PAID);
                        latestTransaction.setPaymentStatus(com.vslearn.entities.Transaction.PaymentStatus.PAID);
                    }
                }
            }
            
            if (latestTransaction == null) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("No valid subscription found")
                        .data(java.util.Map.of(
                            "hasSubscription", false,
                            "userType", "registered",
                            "maxTopics", 2
                        ))
                        .build());
            }
            java.time.Instant now = java.time.Instant.now();
            boolean isValid = now.isAfter(latestTransaction.getStartDate()) && now.isBefore(latestTransaction.getEndDate());
            log.info("Subscription validity check for user {}: now={}, startDate={}, endDate={}, isValid={}", 
                    user.getId(), now, latestTransaction.getStartDate(), latestTransaction.getEndDate(), isValid);
            
            if (isValid) {
                return ResponseEntity.ok(ResponseData.builder()
                        .status(200)
                        .message("Valid subscription found")
                        .data(java.util.Map.of(
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
                        .data(java.util.Map.of(
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

}
