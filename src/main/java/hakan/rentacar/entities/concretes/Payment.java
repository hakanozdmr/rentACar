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
@Table(name = "payments")
public class Payment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime dueDate;

    @Size(max = 100)
    @Column(name = "transaction_id")
    private String transactionId;

    @Size(max = 500)
    @Column(name = "payment_reference")
    private String paymentReference;

    @Size(max = 1000)
    @Column
    private String notes;

    // Payment method enumeration
    public enum PaymentMethod {
        CREDIT_CARD("Kredi Kartı"),
        BANK_TRANSFER("Banka Havalesi"),
        CASH("Nakit"),
        CHECK("Çek"),
        DIGITAL_WALLET("Dijital Cüzdan"),
        ONLINE_BANKING("Online Banking");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Payment status enumeration
    public enum PaymentStatus {
        PENDING("Beklemede"),
        COMPLETED("Tamamlandı"),
        FAILED("Başarısız"),
        CANCELLED("İptal Edildi"),
        REFUNDED("İade Edildi"),
        PARTIAL("Kısmi Ödeme");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}




