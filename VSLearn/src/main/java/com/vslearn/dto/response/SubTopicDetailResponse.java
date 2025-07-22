package com.vslearn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubTopicDetailResponse {
    private Long id;
    private String subTopicName;
    private Long sortOrder;
    private List<VocabDetailResponse> vocabs;
} 