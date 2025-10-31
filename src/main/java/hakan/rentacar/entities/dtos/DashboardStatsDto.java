package hakan.rentacar.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal todayRevenue;
    private Long totalActiveCars;
    private Long totalRentals;
    private Long activeRentals;
    private Long totalCustomers;
    private BigDecimal averageRentalDuration;
    private List<RevenueReportDto> monthlyRevenueData;
    private List<CarAnalyticsDto> topPerformingCars;
    private List<CustomerSegmentDto> customerSegments;
}

