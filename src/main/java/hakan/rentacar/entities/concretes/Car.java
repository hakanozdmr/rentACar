package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
@Entity
@Table(name = "cars")
public class Car extends BaseEntity implements Serializable {

    @NotBlank
    @Pattern(regexp = "^[0-9]{2}[A-ZÇĞIİÖŞÜ]{1,3}[0-9]{2,4}$", message = "Invalid plate format")
    @Column(unique = true)
    private String plate;

    @NotNull
    @Positive
    @Column
    private Double dailyPrice;

    @NotNull
    @Min(value = 2000)
    @Max(value = 2025)
    @Column
    private Integer modelYear;

    @NotNull
    @Min(value = 1)
    @Max(value = 3)
    @Column
    private Integer state; // 1- Available 2-Rented 3-Maintenance

    @NotNull
    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    // Gelişmiş özellikler
    @Column(name = "mileage")
    private Long mileage;

    @Column(name = "fuel_type", length = 20)
    private String fuelType; // Benzin, Dizel, Hibrit, Elektrik

    @Column(name = "transmission", length = 20)
    private String transmission; // Manuel, Otomatik, Yarı Otomatik

    @Column(name = "segment", length = 20)
    private String segment; // Ekonomi, Komfort, Lüks, SUV, Van, Pick-up

    @Column(name = "color", length = 50)
    private String color;

    @ElementCollection
    @CollectionTable(name = "car_features", joinColumns = @JoinColumn(name = "car_id"))
    @Column(name = "feature")
    private List<String> features;

    @ElementCollection
    @CollectionTable(name = "car_images", joinColumns = @JoinColumn(name = "car_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceRecord> maintenanceHistory;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "insurance_company", length = 100)
    private String insuranceCompany;

    @Column(name = "gps_latitude")
    private Double gpsLatitude;

    @Column(name = "gps_longitude")
    private Double gpsLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @OneToMany(mappedBy = "car")
    private List<Rental> rentals;
}
