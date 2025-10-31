package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservation_ratings")
public class ReservationRating extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 1000)
    @Column
    private String comment;

    @Column
    private Boolean carRating;  // Araç değerlendirmesi mi, hizmet değerlendirmesi mi?

    @Column
    private Boolean isPublic;  // Genel yorum mu, yoksa gizli mi?
}

