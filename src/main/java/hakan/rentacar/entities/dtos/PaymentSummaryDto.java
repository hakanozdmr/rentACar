package hakan.rentacar.entities.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentSummaryDto {
    private BigDecimal totalAmount;
    private Long count;
    private BigDecimal pendingAmount;
    private BigDecimal completedAmount;
    private BigDecimal overdueAmount;
    private Map<String, PaymentMethodSummary> byMethod;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PaymentMethodSummary {
        private BigDecimal amount;
        private Long count;
    }
}




