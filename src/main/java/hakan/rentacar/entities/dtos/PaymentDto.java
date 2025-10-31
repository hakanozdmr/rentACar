package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.Payment;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDto {
    private Long id;
    
    @NotNull
    private Long rentalId;
    
    @NotNull
    private Long customerId;
    
    // Customer info for display
    private String customerName;
    private String customerEmail;
    
    // Rental info for display
    private String carPlate;
    private String carBrandName;
    private String carModelName;
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    
    @NotNull
    private Payment.PaymentMethod method;
    
    @NotNull
    private Payment.PaymentStatus status;
    
    private LocalDateTime paidAt;
    private LocalDateTime dueDate;
    private String transactionId;
    private String paymentReference;
    private String notes;
    
    // Additional fields for UI
    private String methodDisplayName;
    private String statusDisplayName;
    private Boolean isOverdue;
    private Long daysUntilDue;
}


