package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.RentalDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Kiralama belgesi bilgileri")
public class RentalDocumentDto {

    private Long id;

    @NotNull
    private Long rentalId;

    @NotBlank
    @Size(max = 200)
    private String fileName;

    @NotBlank
    @Size(max = 50)
    private String fileType;

    @NotNull
    private Long fileSize;

    @NotBlank
    @Size(max = 500)
    private String filePath;

    @NotNull
    private RentalDocument.DocumentType documentType;

    @Size(max = 500)
    private String description;

    private LocalDateTime uploadedAt;

    private String uploadedBy;

    private String thumbnailPath;

    @NotNull
    private Boolean isVerified;

    private LocalDateTime verifiedAt;

    private String verifiedBy;

    private String metadata;

    // Additional fields for response
    private String rentalInfo;
}


