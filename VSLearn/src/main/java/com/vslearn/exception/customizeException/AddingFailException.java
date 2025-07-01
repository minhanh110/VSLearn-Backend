package com.vslearn.exception.customizeException;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddingFailException extends RuntimeException {
    private Object data;

    public AddingFailException(String message, Object data) {
        super(message);
        this.data = data;
    }
}
