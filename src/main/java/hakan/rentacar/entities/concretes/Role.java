package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role extends BaseEntity {

    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;
}

