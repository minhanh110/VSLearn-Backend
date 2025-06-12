package com.vslearn.exception.customizeException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UnAuthenticationException extends RuntimeException {
    private Object data;
    public UnAuthenticationException(String message) {
        super(message);
    }
    public UnAuthenticationException(String message, Throwable cause) {
      super(message, cause);
    }
    public UnAuthenticationException() {
        super("Invalid Token");
    }
}
