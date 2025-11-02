package hakan.rentacar.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GpsLocationDto {
    private Long carId;
    private String plate;
    private String brandName;
    private String modelName;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdate;
    private String address; // Reverse geocoding ile alınan adres
    private Double speed; // km/h (eğer hız sensörü varsa)
    private Boolean isOnline; // Araç online mı?
    private Integer batteryLevel; // GPS cihazı batarya seviyesi
}





