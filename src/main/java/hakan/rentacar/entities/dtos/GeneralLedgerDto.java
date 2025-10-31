package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.GeneralLedger;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralLedgerDto {
    private Long id;
    
    @NotNull
    private GeneralLedger.TransactionType transactionType;
    
    @NotNull
    private GeneralLedger.AccountType accountType;
    
    @NotNull
    private String accountCode;
    
    @NotBlank
    private String accountName;
    
    @NotNull
    private LocalDateTime transactionDate;
    
    private String description;
    
    @NotNull
    private BigDecimal debitAmount;
    
    @NotNull
    private BigDecimal creditAmount;
    
    private Long referenceId;
    private String referenceType;
    private String documentNumber;
    private Boolean reconciled;
    private LocalDateTime reconciledAt;
    
    // Additional fields for UI
    private String transactionTypeDisplayName;
    private String accountTypeDisplayName;
    private BigDecimal balance; // Running balance
    private String relatedEntityInfo; // Related payment/invoice info
}


