package com.vslearn.controller;

import com.vslearn.entities.Transaction;
import com.vslearn.repository.TransactionRepository;
import com.vslearn.repository.PricingRepository;
import com.vslearn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/revenue")
@CrossOrigin(origins = "*", allowCredentials = "false")
@PreAuthorize("hasAuthority('ROLE_GENERAL_MANAGER')")
public class RevenueController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PricingRepository pricingRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy thống kê doanh thu tổng quan
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRevenueStats() {
        try {
            // Lấy tất cả transactions đã thanh toán
            List<Transaction> paidTransactions = transactionRepository.findByPaymentStatus(Transaction.PaymentStatus.PAID);
            
            // Tính tổng doanh thu
            double totalRevenue = paidTransactions.stream()
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                .sum();
            
            // Tính doanh thu theo năm
            Map<Integer, Double> revenueByYear = paidTransactions.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getYear(),
                    Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                ));
            
            // Tính doanh thu theo tháng của năm hiện tại
            int currentYear = LocalDate.now().getYear();
            Map<Integer, Double> revenueByMonth = paidTransactions.stream()
                .filter(t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getYear() == currentYear)
                .collect(Collectors.groupingBy(
                    t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getMonthValue(),
                    Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                ));
            
            // Tính số lượng giao dịch
            long totalTransactions = paidTransactions.size();
            
            // Tính số lượng khách hàng unique
            long uniqueCustomers = paidTransactions.stream()
                .map(t -> t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();
            
            // Tính tổng người đăng ký (tất cả users)
            long totalUsers = userRepository.count();
            
            // Tính người mua gói học (users có transactions)
            long packageBuyers = uniqueCustomers;
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalRevenue", totalRevenue);
            response.put("totalTransactions", totalTransactions);
            response.put("uniqueCustomers", uniqueCustomers);
            response.put("totalUsers", totalUsers);
            response.put("packageBuyers", packageBuyers);
            response.put("revenueByYear", revenueByYear);
            response.put("revenueByMonth", revenueByMonth);
            response.put("status", 200);
            response.put("message", "Lấy thống kê doanh thu thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lấy doanh thu theo khoảng thời gian
     */
    @GetMapping("/by-period")
    public ResponseEntity<Map<String, Object>> getRevenueByPeriod(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month) {
        try {
            List<Transaction> paidTransactions = transactionRepository.findByPaymentStatus(Transaction.PaymentStatus.PAID);
            
            List<Map<String, Object>> revenueData = new ArrayList<>();
            
            if (year != null && !year.equals("all")) {
                int targetYear = Integer.parseInt(year);
                paidTransactions = paidTransactions.stream()
                    .filter(t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getYear() == targetYear)
                    .collect(Collectors.toList());
                
                if (month != null && !month.equals("all")) {
                    int targetMonth = Integer.parseInt(month);
                    paidTransactions = paidTransactions.stream()
                        .filter(t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getMonthValue() == targetMonth)
                        .collect(Collectors.toList());
                    
                    // Nhóm theo tuần trong tháng
                    Map<Integer, Double> weeklyRevenue = paidTransactions.stream()
                        .collect(Collectors.groupingBy(
                            t -> (t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getDayOfMonth() - 1) / 7 + 1,
                            Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                        ));
                    
                    for (int week = 1; week <= 5; week++) {
                        Map<String, Object> weekData = new HashMap<>();
                        weekData.put("period", "Tuần " + week);
                        weekData.put("revenue", weeklyRevenue.getOrDefault(week, 0.0));
                        revenueData.add(weekData);
                    }
                } else {
                    // Nhóm theo tháng trong năm
                    Map<Integer, Double> monthlyRevenue = paidTransactions.stream()
                        .collect(Collectors.groupingBy(
                            t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getMonthValue(),
                            Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                        ));
                    
                    for (int monthNum = 1; monthNum <= 12; monthNum++) {
                        Map<String, Object> monthData = new HashMap<>();
                        monthData.put("period", "Tháng " + monthNum);
                        monthData.put("revenue", monthlyRevenue.getOrDefault(monthNum, 0.0));
                        revenueData.add(monthData);
                    }
                }
            } else {
                // Nhóm theo năm
                Map<Integer, Double> yearlyRevenue = paidTransactions.stream()
                    .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getYear(),
                        Collectors.summingDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                    ));
                
                for (int yearNum = 2022; yearNum <= LocalDate.now().getYear(); yearNum++) {
                    Map<String, Object> yearData = new HashMap<>();
                    yearData.put("period", String.valueOf(yearNum));
                    yearData.put("revenue", yearlyRevenue.getOrDefault(yearNum, 0.0));
                    revenueData.add(yearData);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("revenueData", revenueData);
            response.put("status", 200);
            response.put("message", "Lấy doanh thu theo thời gian thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Lỗi khi lấy doanh thu theo thời gian: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lấy thống kê gói học theo doanh thu
     */
    @GetMapping("/package-stats")
    public ResponseEntity<Map<String, Object>> getPackageRevenueStats(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month) {
        try {
            List<Transaction> paidTransactions = transactionRepository.findByPaymentStatus(Transaction.PaymentStatus.PAID);
            
            // Lọc theo năm và tháng nếu có
            if (year != null && !year.equals("all")) {
                int targetYear = Integer.parseInt(year);
                paidTransactions = paidTransactions.stream()
                    .filter(t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getYear() == targetYear)
                    .collect(Collectors.toList());
                
                if (month != null && !month.equals("all")) {
                    int targetMonth = Integer.parseInt(month);
                    paidTransactions = paidTransactions.stream()
                        .filter(t -> t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).getMonthValue() == targetMonth)
                        .collect(Collectors.toList());
                }
            }
            
            // Nhóm theo pricing type
            Map<String, List<Transaction>> transactionsByPackage = paidTransactions.stream()
                .filter(t -> t.getPricing() != null)
                .collect(Collectors.groupingBy(t -> t.getPricing().getPricingType()));
            
            List<Map<String, Object>> packageStats = new ArrayList<>();
            
            for (Map.Entry<String, List<Transaction>> entry : transactionsByPackage.entrySet()) {
                String packageType = entry.getKey();
                List<Transaction> transactions = entry.getValue();
                
                double totalRevenue = transactions.stream()
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                    .sum();
                
                double packagePrice = transactions.isEmpty() ? 0.0 : 
                    transactions.get(0).getPricing().getPrice() != null ? 
                    transactions.get(0).getPricing().getPrice() : 0.0;
                
                Map<String, Object> packageData = new HashMap<>();
                packageData.put("name", packageType);
                packageData.put("value", transactions.size()); // Số lượng gói bán được
                packageData.put("packageRevenue", totalRevenue);
                packageData.put("packagePrice", packagePrice);
                packageData.put("color", generateRandomColor());
                
                packageStats.add(packageData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("packageStats", packageStats);
            response.put("status", 200);
            response.put("message", "Lấy thống kê gói học thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Lỗi khi lấy thống kê gói học: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lấy doanh thu theo ngày cụ thể
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyRevenue(
            @RequestParam String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date);
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
            
            List<Transaction> paidTransactions = transactionRepository.findByPaymentStatus(Transaction.PaymentStatus.PAID);
            
            List<Transaction> dailyTransactions = paidTransactions.stream()
                .filter(t -> {
                    LocalDateTime transactionTime = t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                    return transactionTime.isAfter(startOfDay) && transactionTime.isBefore(endOfDay);
                })
                .collect(Collectors.toList());
            
            double dailyRevenue = dailyTransactions.stream()
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                .sum();
            
            Map<String, Object> response = new HashMap<>();
            response.put("date", date);
            response.put("revenue", dailyRevenue);
            response.put("transactionCount", dailyTransactions.size());
            response.put("transactions", dailyTransactions);
            response.put("status", 200);
            response.put("message", "Lấy doanh thu theo ngày thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Lỗi khi lấy doanh thu theo ngày: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Tạo màu ngẫu nhiên cho biểu đồ
     */
    private String generateRandomColor() {
        String[] colors = {
            "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6",
            "#06B6D4", "#84CC16", "#F97316", "#EC4899", "#6366F1",
            "#14B8A6", "#F59E0B", "#DC2626", "#7C3AED", "#059669"
        };
        return colors[new Random().nextInt(colors.length)];
    }
} 