package com.vslearn.service.impl;

import com.vslearn.entities.Transaction;
import com.vslearn.entities.User;
import com.vslearn.entities.Pricing;
import com.vslearn.repository.TransactionRepository;
import com.vslearn.repository.UserRepository;
import com.vslearn.repository.PricingRepository;
import com.vslearn.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.vslearn.utils.JwtUtil;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PricingRepository pricingRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Transaction createTransaction(String transactionCode, Double amount, String description) {
        return createTransaction(transactionCode, amount, description, 1L, null); // Default pricing ID = 1, no user ID
    }
    
    @Override
    public Transaction createTransaction(String transactionCode, Double amount, String description, Long pricingId) {
        return createTransaction(transactionCode, amount, description, pricingId, null); // No user ID
    }
    
    @Override
    public Transaction createTransaction(String transactionCode, Double amount, String description, Long pricingId, Long userId) {
        try {
            // Lấy current user
            User currentUser = null;
            
            // Ưu tiên sử dụng userId parameter nếu có
            if (userId != null) {
                currentUser = userRepository.findById(userId).orElse(null);
                log.info("Using provided userId: {}, found user: {}", userId, currentUser != null ? currentUser.getId() : "null");
            }
            
            // Nếu không có userId parameter, thử lấy từ authentication
            if (currentUser == null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                log.info("Authentication: {}", authentication != null ? authentication.getName() : "null");
                log.info("Is authenticated: {}", authentication != null ? authentication.isAuthenticated() : "false");
                
                if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getName())) {
                    try {
                        // Lấy user từ database dựa trên email
                        currentUser = userRepository.findByUserEmail(authentication.getName()).orElse(null);
                        log.info("Found current user from authentication: {}", currentUser != null ? currentUser.getId() : "null");
                    } catch (Exception e) {
                        log.warn("Error getting current user from authentication: {}", e.getMessage());
                    }
                } else {
                    log.warn("Authentication not available or user is anonymous");
                }
                
                // Fallback: thử lấy user ID từ JWT token
                if (currentUser == null) {
                    try {
                        // Lấy token từ request header
                        String authHeader = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                .getRequest().getHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.replace("Bearer ", "");
                            // Parse JWT để lấy user ID
                            String jwtUserId = jwtUtil.getClaimsFromToken(token).getClaims().get("id").toString();
                            currentUser = userRepository.findById(Long.parseLong(jwtUserId)).orElse(null);
                            log.info("Found user from JWT token: {}", currentUser != null ? currentUser.getId() : "null");
                        }
                    } catch (Exception e) {
                        log.warn("Error getting user from JWT: {}", e.getMessage());
                    }
                }
            }
            
            // Final fallback: nếu vẫn không lấy được, sử dụng user mặc định
            if (currentUser == null) {
                log.warn("No current user found, using default user ID = 1");
                currentUser = new User();
                currentUser.setId(1L);
            }
            
            log.info("🔍 Creating transaction for user: {}", currentUser.getId());
            
            // Lấy pricing từ database theo pricingId
            Pricing pricing = pricingRepository.findById(pricingId).orElse(null);
            if (pricing == null) {
                log.error("Pricing with ID {} not found", pricingId);
                throw new RuntimeException("Pricing not found");
            }
            
            Instant now = Instant.now();
            // Tính endDate theo duration_days từ pricing
            Instant endDate = now.plusSeconds(pricing.getDurationDays() * 24 * 60 * 60); // Chuyển ngày thành giây
            
            log.info("Creating transaction with duration: {} days, startDate: {}, endDate: {}", 
                    pricing.getDurationDays(), now, endDate);
            
            Transaction transaction = Transaction.builder()
                    .code(transactionCode)
                    .amount(amount)
                    .description(description)
                    .paymentStatus(Transaction.PaymentStatus.PENDING)
                    .createdBy(currentUser)
                    .pricing(pricing)
                    .startDate(now)
                    .endDate(endDate) // Sử dụng endDate đã tính theo duration_days
                    .createdAt(now)
                    .build();
            
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Created transaction: {}", savedTransaction.getCode());
            
            return savedTransaction;
            
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    @Override
    public Transaction updatePaymentStatus(String transactionCode, Transaction.PaymentStatus status) {
        try {
            Optional<Transaction> optionalTransaction = transactionRepository.findByCode(transactionCode);
            
            if (optionalTransaction.isPresent()) {
                Transaction transaction = optionalTransaction.get();
                transaction.setPaymentStatus(status);
                
                Transaction updatedTransaction = transactionRepository.save(transaction);
                log.info("Updated transaction {} to status: {}", transactionCode, status);
                
                return updatedTransaction;
            } else {
                log.warn("Transaction not found: {}", transactionCode);
                throw new RuntimeException("Transaction not found: " + transactionCode);
            }
            
        } catch (Exception e) {
            log.error("Error updating payment status", e);
            throw new RuntimeException("Failed to update payment status", e);
        }
    }

    @Override
    public Optional<Transaction> findByCode(String transactionCode) {
        return transactionRepository.findByCode(transactionCode);
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> findByPaymentStatus(Transaction.PaymentStatus status) {
        return transactionRepository.findByPaymentStatus(status);
    }

    @Override
    public boolean isTransactionPaid(String transactionCode) {
        Optional<Transaction> transaction = transactionRepository.findByCode(transactionCode);
        return transaction.isPresent() && 
               transaction.get().getPaymentStatus() == Transaction.PaymentStatus.PAID;
    }
} 