package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.ContractTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, Long> {
    
    List<ContractTemplate> findByIsActiveTrue();
    
    Optional<ContractTemplate> findByIsDefaultTrueAndIsActiveTrue();
    
    Optional<ContractTemplate> findByTemplateKey(String templateKey);
    
    @Query("SELECT ct FROM ContractTemplate ct WHERE ct.isActive = true AND ct.isDefault = true")
    Optional<ContractTemplate> findDefaultActiveTemplate();
    
    @Query("SELECT ct FROM ContractTemplate ct WHERE ct.name LIKE %:keyword% OR ct.description LIKE %:keyword%")
    List<ContractTemplate> searchByKeyword(@Param("keyword") String keyword);
}


