package com.vslearn.exception.customizeException;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuthenticationFailException extends RuntimeException {
    private Object data;

    public AuthenticationFailException(String message, Object data) {
        super(message);
    }

    public AuthenticationFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
