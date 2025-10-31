package hakan.rentacar.entities.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceRecordDto {
    private Long id;
    
    @NotNull
    private Long carId;
    
    @NotNull
    private LocalDate maintenanceDate;
    
    @NotBlank
    private String type;
    
    @NotBlank
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal cost;
    
    @NotNull
    @Min(value = 0)
    private Long mileage;
    
    @NotBlank
    private String serviceProvider;
}
