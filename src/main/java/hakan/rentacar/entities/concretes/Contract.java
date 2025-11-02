package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sözleşme yönetimi için entity.
 * Her kiralama için oluşturulacak sözleşme detaylarını saklar.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contracts")
public class Contract extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Column(nullable = false, unique = true)
    private String contractNumber; // Sözleşme numarası (örn: KIR-2024-001)

    @NotNull
    @Column(nullable = false)
    private LocalDate signedDate; // İmza tarihi

    @Column
    private LocalDateTime signedAt; // İmza zamanı (timestamp)

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ContractTemplate template;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String terms; // Sözleşme şartları

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String conditions; // Özel koşullar

    @Size(max = 500)
    @Column
    private String customerSignature; // Müşteri imzası (base64 veya dosya yolu)

    @Size(max = 500)
    @Column
    private String companySignature; // Şirket imzası (base64 veya dosya yolu)

    @Size(max = 100)
    @Column
    private String signedBy; // İmzalayan kişi adı

    @Column
    private LocalDateTime eSignatureVerifiedAt; // E-imza doğrulama zamanı

    @Size(max = 500)
    @Column
    private String eSignatureHash; // E-imza hash değeri (güvenlik)

    @Size(max = 500)
    @Column
    private String pdfPath; // Oluşturulan PDF dosya yolu

    @Size(max = 100)
    @Column
    private String witnessName; // Tanık adı (opsiyonel)

    @Column
    private LocalDate expiryDate; // Sözleşme son geçerlilik tarihi

    @Size(max = 1000)
    @Column
    private String notes; // Ek notlar

    /**
     * Sözleşme durumları
     */
    public enum ContractStatus {
        DRAFT("Taslak"),
        PENDING_SIGNATURE("İmza Bekliyor"),
        SIGNED("İmzalandı"),
        EXPIRED("Süresi Doldu"),
        CANCELLED("İptal Edildi"),
        VERIFIED("Doğrulandı"); // E-imza ile doğrulanmış

        private final String displayName;

        ContractStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

