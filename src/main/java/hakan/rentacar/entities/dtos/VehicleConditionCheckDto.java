package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.VehicleConditionCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Araç durum kontrolü bilgileri")
public class VehicleConditionCheckDto {

    private Long id;

    @NotNull
    private Long rentalId;

    @NotNull
    private Long carId;

    @NotNull
    private VehicleConditionCheck.CheckType checkType;

    @NotNull
    private Long mileageAtCheck;

    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    private Integer fuelLevel;

    @NotNull
    private Boolean bodyHasDamage;

    @Size(max = 1000)
    private String bodyDamageDescription;

    @NotNull
    private Boolean interiorHasDamage;

    @Size(max = 1000)
    private String interiorDamageDescription;

    @NotNull
    private Boolean windowsHaveDamage;

    @Size(max = 500)
    private String windowsDamageDescription;

    @NotNull
    private Boolean tiresHaveDamage;

    @Size(max = 500)
    private String tiresDamageDescription;

    @NotNull
    private Boolean hasScratches;

    @Size(max = 1000)
    private String scratchesDescription;

    @DecimalMin(value = "0.0")
    private BigDecimal damageCost;

    @NotBlank
    @Size(max = 100)
    private String performedBy;

    @NotNull
    private LocalDateTime performedAt;

    @Size(max = 500)
    private String customerNote;

    @Size(max = 500)
    private String staffNote;

    @NotNull
    private Boolean isConfirmed;

    private LocalDateTime confirmedAt;

    @NotNull
    private Boolean needsMaintenance;

    @Size(max = 1000)
    private String maintenanceNote;

    // Additional fields for response
    private String rentalInfo;
    private String carPlate;
}


