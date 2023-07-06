package hakan.rentacar.entities.concretes;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDate;

/**
 * Customer object model (JPA entity).
 *
 * @author Julian Quint
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Column(nullable = false)
    private String street;

    @Min(value = 10000)
    @Max(value = 99999)
    @Column(nullable = false)
    private int zipcode;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String phone;

    @NotBlank
    @Email
    @Column(nullable = false)
    private String email;

    @NotNull
    @Min(value = 18)
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String idNumber;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String driverLicenseNumber;

}
