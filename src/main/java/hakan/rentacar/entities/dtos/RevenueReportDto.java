package hakan.rentacar.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDto {
    private LocalDate date;
    private String period; // MONTHLY, YEARLY
    private BigDecimal totalRevenue;
    private Long totalRentals;
    private BigDecimal averageRevenuePerRental;
    private BigDecimal dailyRate;
    private String carBrand;
    private String carModel;
}

