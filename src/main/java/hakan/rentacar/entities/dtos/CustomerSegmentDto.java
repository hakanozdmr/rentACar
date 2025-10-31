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
public class CustomerSegmentDto {
    private String segmentName; // VIP, REGULAR, NEW
    private Long customerCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenuePerCustomer;
    private BigDecimal averageRentalsPerCustomer;
    private Long totalRentals;
}

