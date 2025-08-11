package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewHistoryEntry {
    private Long id;
    private String action; // "created", "approved", "rejected", "updated"
    private LocalDateTime date;
    private String actor; // Tên người thực hiện
    private String reason; // Lý do (nếu có)
} 