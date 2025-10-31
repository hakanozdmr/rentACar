package hakan.rentacar.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarAnalyticsDto {
    private Long carId;
    private String plate;
    private String brandName;
    private String modelName;
    private Long totalRentals;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private Long totalRentalDays;
    private BigDecimal utilizationRate; // Percentage
    private BigDecimal averageDailyRevenue;
}

