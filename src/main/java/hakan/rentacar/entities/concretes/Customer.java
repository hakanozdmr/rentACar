package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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
@Table(name = "customers")
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
    private Integer zipcode;

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
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String idNumber;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String driverLicenseNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "customer")
    private List<Rental> rentals;
}
