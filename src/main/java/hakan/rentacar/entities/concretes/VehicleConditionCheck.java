package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Araç durum kontrolü için entity.
 * Teslim ve teslim alma işlemlerinde araç durumunu kaydeder.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicle_condition_checks")
public class VehicleConditionCheck extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckType checkType; // TESLIM veya TESLIM_ALMA

    @NotNull
    @Column(nullable = false)
    private Long mileageAtCheck; // Kontrol anındaki kilometre

    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    @Column(nullable = false)
    private Integer fuelLevel; // Yakıt seviyesi (0-100)

    @Builder.Default
    @Column(nullable = false)
    private Boolean bodyHasDamage = false;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String bodyDamageDescription; // Gövde hasarı açıklaması

    @Builder.Default
    @Column(nullable = false)
    private Boolean interiorHasDamage = false;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String interiorDamageDescription; // İç mekan hasarı açıklaması

    @Builder.Default
    @Column(nullable = false)
    private Boolean windowsHaveDamage = false;

    @Size(max = 500)
    @Column
    private String windowsDamageDescription; // Cam hasarı açıklaması

    @Builder.Default
    @Column(nullable = false)
    private Boolean tiresHaveDamage = false;

    @Size(max = 500)
    @Column
    private String tiresDamageDescription; // Lastik hasarı açıklaması

    @Builder.Default
    @Column(nullable = false)
    private Boolean hasScratches = false;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String scratchesDescription; // Çizik açıklaması

    @DecimalMin(value = "0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal damageCost; // Hasar maliyeti (TL)

    @NotNull
    @Size(max = 100)
    @Column(nullable = false)
    private String performedBy; // Kontrolü yapan kişi

    @NotNull
    @Column(nullable = false)
    private LocalDateTime performedAt; // Kontrol zamanı

    @Size(max = 500)
    @Column
    private String customerNote; // Müşteri notu

    @Size(max = 500)
    @Column
    private String staffNote; // Personel notu

    @Builder.Default
    @Column(nullable = false)
    private Boolean isConfirmed = false; // Müşteri onayladı mı

    @Column
    private LocalDateTime confirmedAt; // Onay zamanı

    @Builder.Default
    @Column(nullable = false)
    private Boolean needsMaintenance = false; // Bakım gerekiyor mu

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String maintenanceNote; // Bakım notu

    /**
     * Kontrol tipleri
     */
    public enum CheckType {
        TESLIM("Teslim"),
        TESLIM_ALMA("Teslim Alma");

        private final String displayName;

        CheckType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}


