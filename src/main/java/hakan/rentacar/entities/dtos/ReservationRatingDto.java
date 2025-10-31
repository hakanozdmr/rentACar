package hakan.rentacar.entities.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationRatingDto {
    private Long id;
    
    @NotNull
    private Long rentalId; // Direct rental rating (after table merge)
    
    @NotNull
    private Long customerId;
    
    private String customerName;
    
    // Rental information for display purposes
    private String carBrandName;
    private String carModelName;
    private String carPlate;
    
    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    private Integer rating;
    
    @Size(max = 1000)
    private String comment;
    
    private Boolean carRating;
    
    private Boolean isPublic;
    
    private Date createdDate;
}

