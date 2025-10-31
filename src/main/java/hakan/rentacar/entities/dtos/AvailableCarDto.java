package hakan.rentacar.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableCarDto {
    private Long carId;
    private String carPlate;
    private String carBrandName;
    private String carModelName;
}



