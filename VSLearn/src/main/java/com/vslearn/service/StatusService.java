package com.vslearn.service;

import java.util.List;
import java.util.Map;

public interface StatusService {
    List<Map<String, Object>> getStatusOptions();
    Map<String, Object> addStatusOption(String status, String label);
    String getStatusLabel(String status);
    String getStatusDescription(String status);
} 