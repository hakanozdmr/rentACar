package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.Rental;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
public class RentalDto {

    private Long id;

    @NotNull
    private LocalDate start;

    @NotNull
    private LocalDate end;

    @NotNull
    private Long carId;

    @NotNull
    private Long customerId;

    @PositiveOrZero
    private Integer extraCosts;

    @Size(max = 500)
    private String note;

    // Additional fields for response
    private String carPlate;
    private String carBrandName;
    private String carModelName;
    private String customerFirstName;
    private String customerLastName;
    private Double dailyPrice;
    private Double totalPrice;
    private Boolean canRate; // Can customer rate this rental?
    private Boolean isRated; // Has customer rated this rental?

    // Additional fields from Reservation
    private Rental.RentalStatus status;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
}


