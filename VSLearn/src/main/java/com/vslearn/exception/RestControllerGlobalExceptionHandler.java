package com.vslearn.exception;


import com.vslearn.dto.response.ErrorResponse;
import com.vslearn.exception.customizeException.AddingFailException;
import com.vslearn.exception.customizeException.AuthenticationFailException;
import com.vslearn.exception.customizeException.ResourceNotFoundException;
import com.vslearn.exception.customizeException.UnAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestControllerGlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, AddingFailException.class, AuthenticationFailException.class, UnAuthenticationException.class})
    public ResponseEntity<?> handleException(Exception e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        if(e instanceof ResourceNotFoundException){
             errorResponse = ErrorResponse.builder()
                    .timestamp(new Date())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .path(request.getDescription(true))
                    .error((HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .message(e.getMessage())
                    .build();
        }else if(e instanceof AddingFailException){
            errorResponse = ErrorResponse.builder()
                    .timestamp(new Date())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .path(request.getDescription(true))
                    .error((HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .data(((AddingFailException)e).getData())
                    .message(e.getMessage())
                    .build();
        } else if (e instanceof AuthenticationFailException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(new Date())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .path(request.getDescription(true))
                    .error((HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .data(((AuthenticationFailException)e).getData())
                    .message(e.getMessage())
                    .build();
        } else if (e instanceof UnAuthenticationException) {
            errorResponse = ErrorResponse.builder()
                    .timestamp(new Date())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .path(request.getDescription(true))
                    .error((HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .data(((UnAuthenticationException)e).getData())
                    .message(e.getMessage())
                    .build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e, WebRequest request) {
        // Get first validation error message
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(true))
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errorMessage)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}