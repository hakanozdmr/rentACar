package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.concretes.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepostory extends JpaRepository<Model,Long> {
}
