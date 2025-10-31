package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.*;
import hakan.rentacar.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Analytics", description = "Analiz ve raporlama")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // Revenue Reports
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<RevenueReportDto>> getMonthlyRevenue(@RequestParam int year) {
        List<RevenueReportDto> revenue = analyticsService.getMonthlyRevenueReport(year);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/yearly")
    public ResponseEntity<List<RevenueReportDto>> getYearlyRevenue(
            @RequestParam int startYear, 
            @RequestParam int endYear) {
        List<RevenueReportDto> revenue = analyticsService.getYearlyRevenueReport(startYear, endYear);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<RevenueReportDto>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RevenueReportDto> revenue = analyticsService.getDailyRevenueReport(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    // Car Analytics
    @GetMapping("/cars/most-rented")
    public ResponseEntity<List<CarAnalyticsDto>> getMostRentedCars(@RequestParam(defaultValue = "10") int limit) {
        List<CarAnalyticsDto> cars = analyticsService.getMostRentedCars(limit);
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/cars/top-revenue")
    public ResponseEntity<List<CarAnalyticsDto>> getTopRevenueCars(@RequestParam(defaultValue = "10") int limit) {
        List<CarAnalyticsDto> cars = analyticsService.getTopRevenueCars(limit);
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/cars/utilization")
    public ResponseEntity<List<CarAnalyticsDto>> getCarUtilizationStats() {
        List<CarAnalyticsDto> cars = analyticsService.getCarUtilizationStats();
        return ResponseEntity.ok(cars);
    }

    // Customer Segmentation
    @GetMapping("/customers/segmentation")
    public ResponseEntity<List<CustomerSegmentDto>> getCustomerSegmentation() {
        List<CustomerSegmentDto> segments = analyticsService.getCustomerSegmentation();
        return ResponseEntity.ok(segments);
    }

    // Dashboard Statistics
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // Trend Analysis
    @GetMapping("/trends/revenue")
    public ResponseEntity<List<TrendAnalysisDto>> getRevenueTrend(@RequestParam(defaultValue = "12") int months) {
        List<TrendAnalysisDto> trends = analyticsService.getRevenueTrend(months);
        return ResponseEntity.ok(trends);
    }

    // Real-time Statistics
    @GetMapping("/realtime/today-revenue")
    public ResponseEntity<String> getTodayRevenue() {
        return ResponseEntity.ok(analyticsService.getTodayRevenue().toString());
    }

    @GetMapping("/realtime/active-rentals")
    public ResponseEntity<Long> getActiveRentalsCount() {
        return ResponseEntity.ok(analyticsService.getActiveRentalsCount());
    }

    @GetMapping("/realtime/avg-rental-duration")
    public ResponseEntity<String> getAverageRentalDuration() {
        return ResponseEntity.ok(analyticsService.getAverageRentalDuration().toString());
    }
}
