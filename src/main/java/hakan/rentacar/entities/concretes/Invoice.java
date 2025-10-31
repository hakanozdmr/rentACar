package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Size(max = 50)
    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime issueDate;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dueDate;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate; // VAT rate as decimal (0.18 for 18%)

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Size(max = 500)
    @Column(name = "payment_terms")
    private String paymentTerms;

    @Size(max = 1000)
    @Column(name = "notes")
    private String notes;

    @Size(max = 100)
    @Column(name = "reference_number")
    private String referenceNumber;

    // Invoice status enumeration
    public enum InvoiceStatus {
        PENDING("Beklemede"),
        SENT("Gönderildi"),
        PAID("Ödendi"),
        OVERDUE("Vadesi Geçti"),
        CANCELLED("İptal Edildi");

        private final String displayName;

        InvoiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}


