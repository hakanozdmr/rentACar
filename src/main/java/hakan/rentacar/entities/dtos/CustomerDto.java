package hakan.rentacar.entities.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
public class CustomerDto {

    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String street;

    @Min(value = 10000)
    @Max(value = 99999)
    private Integer zipcode;

    @NotBlank
    private String city;

    @NotBlank
    @Pattern(regexp = "^[0-9\\-\\+\\s\\(\\)]{10,}$", message = "Invalid phone format")
    private String phone;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Invalid ID number format")
    private String idNumber;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{8,}$", message = "Invalid driver license format")
    private String driverLicenseNumber;
}


