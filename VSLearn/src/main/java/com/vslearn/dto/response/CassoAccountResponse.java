package com.vslearn.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CassoAccountResponse {
    private int error;
    private String message;
    private List<CassoAccount> data;

    @Data
    public static class CassoAccount {
        private Long id;
        private String name;
        @JsonProperty("bank_name")
        private String bankName;
        @JsonProperty("account_number")
        private String accountNumber;
        @JsonProperty("account_name")
        private String accountName;
        @JsonProperty("bank_code_name")
        private String bankCodeName;
        private String status;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("updated_at")
        private String updatedAt;
    }
} 