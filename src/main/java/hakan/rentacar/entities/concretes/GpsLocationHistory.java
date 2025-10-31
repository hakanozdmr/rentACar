package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "gps_location_history")
public class GpsLocationHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @NotNull
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @NotNull
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "address")
    private String address; // Reverse geocoding ile alınan adres

    @Column(name = "speed")
    private Double speed; // km/h

    @Column(name = "is_online")
    private Boolean isOnline = true;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "source", length = 50)
    private String source; // "GPS_DEVICE", "MOBILE_APP", "MANUAL"
}
