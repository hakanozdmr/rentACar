package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.concretes.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelRepostory extends JpaRepository<Model,Long> {
    
    List<Model> findByBrandId(Long brandId);
    
    boolean existsByNameAndBrandId(String name, Long brandId);
}
