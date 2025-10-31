package hakan.rentacar.entities.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Araç markası bilgileri")
public class BrandDto {

    @Schema(description = "Marka ID", example = "1")
    private Long id;
    
    @NotNull
    @NotBlank
    @Size(min = 3,max = 20)
    @Schema(description = "Marka adı", example = "Toyota", minLength = 3, maxLength = 20)
    private String name;
}
