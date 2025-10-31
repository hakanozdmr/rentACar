package hakan.rentacar.entities.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
@Schema(description = "Araç bilgileri")
public class CarDto {

    private Long id;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{2}[A-ZÇĞIİÖŞÜ]{1,3}[0-9]{2,4}$", message = "Invalid plate format")
    private String plate;
    
    @NotNull
    @Positive
    private Double dailyPrice;
    
    @NotNull
    @Min(value = 2000)
    @Max(value = 2025)
    private Integer modelYear;
    
    @NotNull
    @Min(value = 1)
    @Max(value = 3)
    private Integer state; // 1- Available 2-Rented 3-Maintenance
    
    @NotNull
    private Long modelId;
    
    // Additional fields for response
    private String modelName;
    private String brandName;
    
    // Gelişmiş özellikler
    private Long mileage;
    private String fuelType;
    private String transmission;
    private String segment;
    private String color;
    private List<String> features;
    private List<String> images;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private List<MaintenanceRecordDto> maintenanceHistory;
    private LocalDate insuranceExpiryDate;
    private String insuranceCompany;
    private Double gpsLatitude;
    private Double gpsLongitude;
    private LocalDateTime lastLocationUpdate;
    
    // Rating bilgileri
    private Double averageRating;
    private Long ratingCount;
    private List<ReservationRatingDto> ratings;
}

