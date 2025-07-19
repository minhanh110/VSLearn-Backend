package com.vslearn.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String userEmail;
    private String phoneNumber;
    private String userRole;
    private String userAvatar;
    private Boolean isActive;
    private String provider;
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
    
    // Additional stats for different roles
    private Long topicsCompleted; // For learners
    private Long packagesOwned; // For learners
    private Long topicsCreated; // For creators
    private Long vocabularyCreated; // For creators
    private Long pendingApproval; // For creators
    private Long topicsApproved; // For approvers
    private Long pendingReview; // For approvers
    private String specialization; // For creators/approvers
} 