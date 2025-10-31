package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull
    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @NotBlank
    @Column(name = "maintenance_type", length = 100)
    private String type; // Periyodik, Fren, Motor, Lastik, vs.

    @NotBlank
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @NotNull
    @Min(value = 0)
    @Column(name = "mileage_at_service")
    private Long mileage;

    @NotBlank
    @Column(name = "service_provider", length = 200)
    private String serviceProvider;
}
