package hakan.rentacar.entities.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Sözleşme şablonu bilgileri")
public class ContractTemplateDto {

    private Long id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String templateKey;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isActive;

    @NotNull
    private Boolean isDefault;

    private Integer version;

    private String variables;

    private LocalDateTime lastUsedAt;

    private Long usageCount;
}


