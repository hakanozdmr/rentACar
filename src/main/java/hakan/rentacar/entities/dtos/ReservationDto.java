package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Rezervasyon bilgileri")
public class ReservationDto {
    private Long id;
    
    @NotNull
    private Long customerId;
    
    private String customerName;
    
    @NotNull
    private Long carId;
    
    private String carPlate;
    private String carBrandName;
    private String carModelName;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    private Reservation.ReservationStatus status;
    
    private BigDecimal totalAmount;
    
    @Size(max = 500)
    private String specialRequests;
    
    @Size(max = 500)
    private String note;
    
    private LocalDateTime confirmedAt;
    
    private LocalDateTime cancelledAt;
    
    private Long daysCount;
}
