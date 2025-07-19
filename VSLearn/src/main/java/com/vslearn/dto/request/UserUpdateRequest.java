package com.vslearn.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Size(max = 255, message = "Username must not exceed 255 characters")
    private String userName;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String userEmail;
    
    @Size(max = 12, message = "Phone number must not exceed 12 characters")
    private String phoneNumber;
    
    @Size(max = 255, message = "User role must not exceed 255 characters")
    private String userRole;
    
    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String userAvatar;
    
    private Boolean isActive;
} 