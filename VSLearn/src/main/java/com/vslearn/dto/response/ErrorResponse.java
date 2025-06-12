package com.vslearn.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse implements Serializable {
    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private Object data;
    private String message;
}
