package com.vslearn.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @NotBlank(message = "Username is required")
    @Size(max = 255, message = "Username must not exceed 255 characters")
    private String userName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String userEmail;
    
    @Size(max = 12, message = "Phone number must not exceed 12 characters")
    private String phoneNumber;
    
    @NotBlank(message = "User role is required")
    @Size(max = 255, message = "User role must not exceed 255 characters")
    private String userRole; // LEARNER, CONTENT_CREATOR, CONTENT_APPROVER, GENERAL_MANAGER
    
    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String userAvatar;
    
    @Builder.Default
    private Boolean isActive = true;
} 