package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.UniqueElements;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
@Entity
@Table(name = "cars")
public class Car extends BaseEntity implements Serializable {

    @Column(unique = true)
    private String plate;

    @Column
    private double dailyPrice;

    @Column
    private int modelYear;

    @Column
    private int state; // 1- Available 2-Rented 3-Maintence

    @ManyToOne
    @JoinColumn(name = "model_id")
    private Model model;


}
