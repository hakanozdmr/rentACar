package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.Invoice;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDto {
    private Long id;
    
    @NotNull
    private Long rentalId;
    
    @NotNull
    private Long customerId;
    
    // Customer info for display
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    
    // Rental info for display
    private String carPlate;
    private String carBrandName;
    private String carModelName;
    private LocalDateTime rentalStartDate;
    private LocalDateTime rentalEndDate;
    private Long rentalDays;
    
    @NotBlank
    private String invoiceNumber;
    
    @NotNull
    private LocalDateTime issueDate;
    
    @NotNull
    private LocalDateTime dueDate;
    
    @NotNull
    private BigDecimal subtotal;
    
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    
    @NotNull
    private BigDecimal totalAmount;
    
    @NotNull
    private Invoice.InvoiceStatus status;
    
    private LocalDateTime paidAt;
    private String paymentTerms;
    private String notes;
    private String referenceNumber;
    
    // Additional fields for UI
    private String statusDisplayName;
    private Boolean isOverdue;
    private Long daysUntilDue;
    private Long daysOverdue;
}


