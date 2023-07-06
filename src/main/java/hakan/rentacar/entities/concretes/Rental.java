package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private LocalDate start;

    @NotNull
    @Column(nullable = false)
    private LocalDate end;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @PositiveOrZero
    @Column
    private int extraCosts;

    @Column
    private String note;

}