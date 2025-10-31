package hakan.rentacar.entities.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceSummaryDto {
    private BigDecimal totalAmount;
    private Long count;
    private BigDecimal pendingAmount;
    private BigDecimal paidAmount;
    private BigDecimal overdueAmount;
    private Map<String, InvoiceStatusSummary> byStatus;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InvoiceStatusSummary {
        private BigDecimal amount;
        private Long count;
    }
}


