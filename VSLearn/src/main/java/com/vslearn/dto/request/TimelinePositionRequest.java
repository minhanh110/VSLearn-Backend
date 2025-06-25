package com.vslearn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimelinePositionRequest {
    private String userId;
    private Integer timelinePosition;
    private String action; // "next", "prev", "reset", "goto_practice"
} 