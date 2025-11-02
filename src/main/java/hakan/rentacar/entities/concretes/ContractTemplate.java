package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Sözleşme şablonları için entity.
 * Yeniden kullanılabilir sözleşme şablonlarını saklar.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contract_templates")
public class ContractTemplate extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name; // Şablon adı (örn: "Standart Kiralama Sözleşmesi")

    @Size(max = 500)
    @Column
    private String description; // Şablon açıklaması

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String templateKey; // Şablon anahtarı (örn: "STANDARD_RENTAL", "LUXURY_RENTAL")

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Şablon içeriği (HTML veya markdown)

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column
    private Integer version; // Şablon versiyonu

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String variables; // Kullanılabilir değişkenler JSON formatında

    @Column
    private LocalDateTime lastUsedAt; // Son kullanım zamanı

    @Column
    private Long usageCount; // Kullanım sayısı

    /**
     * Değişkenler JSON formatında saklanacak.
     * Örnek: {"customerName": "Müşteri Adı", "rentalStart": "Kiralama Başlangıç", ...}
     */
}


