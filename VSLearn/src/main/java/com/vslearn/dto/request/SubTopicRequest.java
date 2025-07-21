package com.vslearn.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubTopicRequest {
    private String subTopicName;
    private Long sortOrder;
    private List<VocabCreateRequest> vocabs;
} 