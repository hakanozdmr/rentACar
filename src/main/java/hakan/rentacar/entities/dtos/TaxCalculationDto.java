package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.TaxCalculation;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxCalculationDto {
    private Long id;
    
    private Long invoiceId;
    private Long paymentId;
    
    @NotNull
    private TaxCalculation.TaxType taxType;
    
    @NotNull
    private BigDecimal taxableAmount;
    
    @NotNull
    private BigDecimal taxRate;
    
    @NotNull
    private BigDecimal taxAmount;
    
    @NotNull
    private LocalDateTime calculationDate;
    
    private String calculationDetails;
    private Boolean isReported;
    private LocalDateTime reportedAt;
    private String taxPeriod;
    
    // Additional fields for UI
    private String taxTypeDisplayName;
    private BigDecimal taxPercentage; // taxRate * 100 for display
    private String relatedEntityInfo;
}


