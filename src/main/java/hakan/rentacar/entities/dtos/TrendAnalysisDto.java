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
public class TrendAnalysisDto {
    private LocalDate date;
    private BigDecimal revenue;
    private Long rentalCount;
    private BigDecimal growthRate; // Percentage compared to previous period
    private String trend; // UP, DOWN, STABLE
}

