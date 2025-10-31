package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    
    // Revenue Reports
    List<RevenueReportDto> getMonthlyRevenueReport(int year);
    List<RevenueReportDto> getYearlyRevenueReport(int startYear, int endYear);
    List<RevenueReportDto> getDailyRevenueReport(LocalDate startDate, LocalDate endDate);
    
    // Car Analytics
    List<CarAnalyticsDto> getMostRentedCars(int limit);
    List<CarAnalyticsDto> getTopRevenueCars(int limit);
    List<CarAnalyticsDto> getCarUtilizationStats();
    
    // Customer Segmentation
    List<CustomerSegmentDto> getCustomerSegmentation();
    
    // Dashboard Statistics
    DashboardStatsDto getDashboardStats();
    List<TrendAnalysisDto> getRevenueTrend(int months);
    
    // Real-time Statistics
    BigDecimal getTodayRevenue();
    Long getActiveRentalsCount();
    BigDecimal getAverageRentalDuration();
}
