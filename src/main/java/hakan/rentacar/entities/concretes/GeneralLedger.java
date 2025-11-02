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
@Table(name = "general_ledger")
public class GeneralLedger extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull
    @Size(max = 100)
    @Column(name = "account_code", nullable = false)
    private String accountCode;

    @NotBlank
    @Size(max = 200)
    @Column(name = "account_name", nullable = false)
    private String accountName;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Size(max = 1000)
    @Column(name = "description")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "debit_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "credit_amount", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "reference_id")
    private Long referenceId; // Related to payment, invoice, rental, etc.

    @Size(max = 50)
    @Column(name = "reference_type")
    private String referenceType; // PAYMENT, INVOICE, RENTAL, EXPENSE, etc.

    @Size(max = 100)
    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "reconciled")
    @Builder.Default
    private Boolean reconciled = false;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    // Transaction Type Enum
    public enum TransactionType {
        REVENUE("Gelir"),
        EXPENSE("Gider"),
        ASSET("Varlık"),
        LIABILITY("Yükümlülük"),
        EQUITY("Özkaynak"),
        TRANSFER("Transfer");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Account Type Enum
    public enum AccountType {
        // Assets (Varlıklar)
        CASH_ASSET("Kasa"),
        BANK_ASSET("Banka"),
        ACCOUNTS_RECEIVABLE("Alacaklar"),
        INVENTORY_ASSET("Stok"),
        FIXED_ASSET("Duran Varlık"),

        // Liabilities (Yükümlülükler)
        ACCOUNTS_PAYABLE("Borçlar"),
        TAX_PAYABLE("Vergi Borcu"),
        SHORT_TERM_DEBT("Kısa Vadeli Borç"),
        LONG_TERM_DEBT("Uzun Vadeli Borç"),

        // Equity (Özkaynaklar)
        CAPITAL("Sermaye"),
        RETAINED_EARNINGS("Yedek Akçe"),

        // Revenue (Gelirler)
        RENTAL_REVENUE("Kiralama Geliri"),
        OTHER_REVENUE("Diğer Gelirler"),

        // Expenses (Giderler)
        OPERATING_EXPENSE("İşletme Gideri"),
        MAINTENANCE_EXPENSE("Bakım Gideri"),
        ADMINISTRATIVE_EXPENSE("İdari Gider"),
        TAX_EXPENSE("Vergi Gideri"),
        FUEL_EXPENSE("Yakıt Gideri"),
        INSURANCE_EXPENSE("Sigorta Gideri");

        private final String displayName;

        AccountType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}




