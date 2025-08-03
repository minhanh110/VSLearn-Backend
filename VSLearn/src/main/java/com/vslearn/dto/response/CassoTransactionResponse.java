package com.vslearn.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CassoTransactionResponse {
    private int error;
    private String message;
    private CassoData data;

    @Data
    public static class CassoData {
        private int page;
        private int pageSize;
        private Integer nextPage;
        private Integer prevPage;
        private int totalPages;
        private int totalRecords;
        private List<CassoTransaction> records;
    }

    @Data
    public static class CassoTransaction {
        private Long id;
        private String tid;
        private String description;
        private double amount;
        @JsonProperty("cusumBalance")
        private Double cusumBalance;
        private String when;
        @JsonProperty("bookingDate")
        private String bookingDate;
        private String bankSubAccId;
        private String paymentChannel;
        private String virtualAccount;
        private String virtualAccountName;
        private String corresponsiveName;
        private String corresponsiveAccount;
        private String corresponsiveBankId;
        private String corresponsiveBankName;
        private Long accountId;
        private String bankCodeName;
    }
} 