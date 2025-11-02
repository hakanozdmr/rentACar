package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Kiralama ile ilgili belgeler ve fotoğraflar için entity.
 * Araç fotoğrafları, hasar raporları, imzalar vb. saklanır.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rental_documents")
public class RentalDocument extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String fileName; // Dosya adı

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String fileType; // Dosya tipi (PDF, JPG, PNG, etc.)

    @NotNull
    @Column(nullable = false)
    private Long fileSize; // Dosya boyutu (byte)

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false)
    private String filePath; // Dosya yolu veya URL

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Size(max = 500)
    @Column
    private String description; // Belge açıklaması

    @Column
    private LocalDateTime uploadedAt; // Yükleme zamanı

    @Size(max = 100)
    @Column
    private String uploadedBy; // Yükleyen kişi

    @Size(max = 500)
    @Column
    private String thumbnailPath; // Küçük resim yolu (fotoğraflar için)

    @Builder.Default
    @Column(nullable = false)
    private Boolean isVerified = false; // Belge doğrulandı mı

    @Column
    private LocalDateTime verifiedAt; // Doğrulama zamanı

    @Size(max = 100)
    @Column
    private String verifiedBy; // Doğrulayan kişi

    @Size(max = 1000)
    @Column
    private String metadata; // Ek veriler JSON formatında

    /**
     * Belge tipleri
     */
    public enum DocumentType {
        DELIVERY_PHOTO("Teslim Fotoğrafı"),
        PICKUP_PHOTO("Teslim Alma Fotoğrafı"),
        DAMAGE_REPORT("Hasar Raporu"),
        CONTRACT("Sözleşme"),
        ID_CARD("Kimlik Fotokopisi"),
        DRIVER_LICENSE("Ehliyet Fotokopisi"),
        INSURANCE("Sigorta Belgesi"),
        CONDITION_CHECK("Durum Kontrol Formu"),
        SIGNATURE("İmza"),
        OTHER("Diğer");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}


