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
@Table(name = "tax_calculations")
public class TaxCalculation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false)
    private TaxType taxType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "taxable_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal taxableAmount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    @Column(name = "tax_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal taxRate; // 0.18 for 18%

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "tax_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal taxAmount;

    @NotNull
    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    @Size(max = 500)
    @Column(name = "calculation_details")
    private String calculationDetails;

    @Column(name = "is_reported")
    @Builder.Default
    private Boolean isReported = false;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Size(max = 100)
    @Column(name = "tax_period")
    private String taxPeriod; // YYYY-MM format

    // Tax Type Enum
    public enum TaxType {
        VAT("KDV", BigDecimal.valueOf(0.18)), // 18% VAT
        CORPORATE_TAX("Kurumlar Vergisi", BigDecimal.valueOf(0.20)), // 20% Corporate Tax
        WITHHOLDING_TAX("Stopaj", BigDecimal.valueOf(0.015)), // 1.5% Withholding Tax
        STAMP_TAX("Damga Vergisi", BigDecimal.valueOf(0.0075)), // 0.75% Stamp Tax
        MUNICIPAL_TAX("Belediye Vergisi", BigDecimal.valueOf(0.002)); // 0.2% Municipal Tax

        private final String displayName;
        private final BigDecimal defaultRate;

        TaxType(String displayName, BigDecimal defaultRate) {
            this.displayName = displayName;
            this.defaultRate = defaultRate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public BigDecimal getDefaultRate() {
            return defaultRate;
        }
    }
}




