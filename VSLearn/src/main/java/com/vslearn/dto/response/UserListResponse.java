package com.vslearn.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<UserManagementResponse> users;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    // Statistics
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long learnersCount;
    private long creatorsCount;
    private long approversCount;
    private long managersCount;
} 