package com.vslearn.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.vslearn.dto.response.CassoTransactionResponse;
import com.vslearn.dto.response.CassoAccountResponse;
import com.vslearn.service.CassoService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CassoServiceImpl implements CassoService {

    @Value("${casso.api.url}")
    private String cassoApiUrl;

    @Value("${casso.api.key}")
    private String cassoApiKey;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<CassoTransactionResponse.CassoTransaction> getTransactions(LocalDate fromDate, LocalDate toDate) {
        try {
            String fromDateStr = fromDate.format(dateFormatter);
            String toDateStr = toDate.format(dateFormatter);
            
            String url = String.format("%s/transactions?fromDate=%s&toDate=%s&page=1&pageSize=50&sort=DESC",
                    cassoApiUrl, fromDateStr, toDateStr);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Apikey " + cassoApiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();

            log.info("Calling Casso API: {}", url);

            try (Response response = httpClient.newCall(request).execute()) {
                log.info("Casso API response code: {}", response.code());
                log.info("Casso API response message: {}", response.message());
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    log.error("Casso API failed: {} - {} - Body: {}", response.code(), response.message(), errorBody);
                    return new ArrayList<>();
                }

                String responseBody = response.body().string();
                log.info("Casso API response: {}", responseBody);

                try {
                    CassoTransactionResponse cassoResponse = objectMapper.readValue(responseBody, CassoTransactionResponse.class);
                    log.info("Successfully parsed Casso response with {} records", 
                            cassoResponse.getData() != null ? cassoResponse.getData().getRecords().size() : 0);
                    
                    if (cassoResponse.getError() == 0 && cassoResponse.getData() != null) {
                        return cassoResponse.getData().getRecords();
                    } else {
                        log.error("Casso API error: {} - {}", cassoResponse.getError(), cassoResponse.getMessage());
                        return new ArrayList<>();
                    }
                } catch (Exception parseError) {
                    log.error("Error parsing Casso API response: {}", parseError.getMessage());
                    throw parseError;
                }
            }
        } catch (IOException e) {
            log.error("Error calling Casso API", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean checkPaymentStatus(String transactionCode, double expectedAmount) {
        try {
            // Lấy giao dịch trong 7 ngày gần nhất
            LocalDate fromDate = LocalDate.now().minusDays(7);
            LocalDate toDate = LocalDate.now();
            
            log.info("Checking payment for transaction code: {} with expected amount: {}", transactionCode, expectedAmount);
            
            // Retry logic với delay để tránh rate limiting
            List<CassoTransactionResponse.CassoTransaction> transactions = null;
            int retryCount = 0;
            int maxRetries = 3;
            
            while (retryCount < maxRetries) {
                try {
                    transactions = getTransactions(fromDate, toDate);
                    break; // Thành công, thoát loop
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.warn("Casso API failed, retrying in {} seconds... (attempt {}/{})", retryCount * 10, retryCount, maxRetries);
                        TimeUnit.SECONDS.sleep(retryCount * 10); // Delay tăng dần: 10s, 20s, 30s
                    } else {
                        log.error("Casso API failed after {} attempts", maxRetries);
                        return false;
                    }
                }
            }
            
            if (transactions == null) {
                log.error("Failed to get transactions from Casso API");
                return false;
            }
            
            log.info("Found {} transactions from Casso API", transactions.size());
            
            // Log tất cả transactions để debug
            for (CassoTransactionResponse.CassoTransaction transaction : transactions) {
                log.info("Casso Transaction: ID={}, Amount={}, Description={}, When={}", 
                        transaction.getId(), transaction.getAmount(), transaction.getDescription(), transaction.getWhen());
            }
            
            // Tính thời gian hiện tại để so sánh
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);
            
            log.info("Looking for transactions from {} to {} (within last 30 minutes)", thirtyMinutesAgo, now);
            
            for (CassoTransactionResponse.CassoTransaction transaction : transactions) {
                double transactionAmount = Math.abs(transaction.getAmount());
                
                // Kiểm tra amount trước
                if (Math.abs(transactionAmount - expectedAmount) < 1000) { // Cho phép sai số 1000 VND
                    log.info("Amount matches! Transaction ID={}, Amount={}, Description={}", 
                            transaction.getId(), transaction.getAmount(), transaction.getDescription());
                    
                    // Kiểm tra thời gian (trong vòng 30 phút gần nhất)
                    if (transaction.getWhen() != null) {
                        LocalDateTime transactionTime = LocalDateTime.parse(transaction.getWhen(), 
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        
                        log.info("Transaction time: {}, 30 minutes ago: {}, Is recent: {}", 
                                transactionTime, thirtyMinutesAgo, transactionTime.isAfter(thirtyMinutesAgo));
                        
                        if (transactionTime.isAfter(thirtyMinutesAgo)) {
                            log.info("✅ Payment confirmed! Transaction ID={}, Amount={}, Time={}, Description={}", 
                                    transaction.getId(), transaction.getAmount(), transaction.getWhen(), transaction.getDescription());
                            return true;
                        } else {
                            log.info("❌ Transaction too old: {}", transaction.getWhen());
                        }
                    } else {
                        log.warn("Transaction has no timestamp: {}", transaction.getId());
                    }
                }
            }
            
            log.info("Payment not found for amount: {} in last 30 minutes", expectedAmount);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return false;
        }
    }

    @Override
    public CassoTransactionResponse.CassoTransaction getTransactionById(Long transactionId) {
        try {
            String url = String.format("%s/transactions/%d", cassoApiUrl, transactionId);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Apikey " + cassoApiKey)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

            log.info("Calling Casso API for transaction: {}", url);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Casso API failed: {} - {}", response.code(), response.message());
                    return null;
                }

                String responseBody = response.body().string();
                log.info("Casso API response: {}", responseBody);

                CassoTransactionResponse cassoResponse = objectMapper.readValue(responseBody, CassoTransactionResponse.class);
                
                if (cassoResponse.getError() == 0 && cassoResponse.getData() != null) {
                    // Parse single transaction from data
                    return objectMapper.convertValue(cassoResponse.getData(), CassoTransactionResponse.CassoTransaction.class);
                } else {
                    log.error("Casso API error: {} - {}", cassoResponse.getError(), cassoResponse.getMessage());
                    return null;
                }
            }
        } catch (IOException e) {
            log.error("Error calling Casso API for transaction", e);
            return null;
        }
    }

    @Override
    public List<CassoAccountResponse.CassoAccount> getAccounts() {
        try {
            String url = String.format("%s/accounts", cassoApiUrl);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Apikey " + cassoApiKey)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

            log.info("Calling Casso API for accounts: {}", url);

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Casso API failed: {} - {}", response.code(), response.message());
                    return new ArrayList<>();
                }

                String responseBody = response.body().string();
                log.info("Casso API response: {}", responseBody);

                CassoAccountResponse cassoResponse = objectMapper.readValue(responseBody, CassoAccountResponse.class);
                
                if (cassoResponse.getError() == 0 && cassoResponse.getData() != null) {
                    return cassoResponse.getData();
                } else {
                    log.error("Casso API error: {} - {}", cassoResponse.getError(), cassoResponse.getMessage());
                    return new ArrayList<>();
                }
            }
        } catch (IOException e) {
            log.error("Error calling Casso API for accounts", e);
            return new ArrayList<>();
        }
    }
} 