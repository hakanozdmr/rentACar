package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepostory extends JpaRepository<Brand,Long> {

    boolean existsByName(String name);
//    List<Brand> getAll();
}
