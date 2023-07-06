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
public class BrandDto {

    private Long id;
    @NotNull
    @NotBlank
    @Size(min = 3,max = 20)
    private String name;
}
