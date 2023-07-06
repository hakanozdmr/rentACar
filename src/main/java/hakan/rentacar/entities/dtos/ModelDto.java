package hakan.rentacar.entities.dtos;

import lombok.*;
import lombok.extern.log4j.Log4j2;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
public class ModelDto {

    private Long id;
    private String name;
    private Long brandId;
}
