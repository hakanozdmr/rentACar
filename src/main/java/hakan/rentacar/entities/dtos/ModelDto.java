package hakan.rentacar.entities.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.log4j.Log4j2;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
public class ModelDto {

    private Long id;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    
    @NotNull
    private Long brandId;
    
    // Additional field for response
    private String brandName;
}
