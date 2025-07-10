package com.vslearn.service;

import com.vslearn.dto.response.AdminDashboardResponse;

import java.util.Map;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();
    Map<String, Object> getUserStats();
    Map<String, Object> getContentStats();
    Map<String, Object> getLearningStats();
} 