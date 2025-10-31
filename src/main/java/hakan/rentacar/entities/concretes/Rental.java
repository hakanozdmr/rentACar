package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rentals")
public class Rental extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private LocalDate start;

    @NotNull
    @Column(name = "\"end\"", nullable = false)
    private LocalDate end;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @PositiveOrZero
    @Column
    private Integer extraCosts;

    @Size(max = 500)
    @Column
    private String note;

    // Additional fields from Reservation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RentalStatus status = RentalStatus.CONFIRMED;

    @DecimalMin(value = "0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Size(max = 500)
    @Column
    private String specialRequests;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime cancelledAt;

    // Inner enum for RentalStatus (similar to ReservationStatus)
    public enum RentalStatus {
        PENDING,    // Beklemede
        CONFIRMED,  // Onaylandı
        CANCELLED,  // İptal edildi
        COMPLETED,  // Tamamlandı
        EXPIRED     // Süresi doldu
    }
}