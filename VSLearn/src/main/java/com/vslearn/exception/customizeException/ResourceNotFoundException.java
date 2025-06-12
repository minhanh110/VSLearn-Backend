package com.vslearn.exception.customizeException;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResourceNotFoundException extends RuntimeException {
    private Object data;
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(String message, Object data) {
        super(message);
        this.data = data;
    }
}
