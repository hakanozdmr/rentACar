package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    boolean existsByPlate(String plate);
    
    List<Car> findByModelId(Long modelId);
    
    List<Car> findByState(Integer state);
    
    @Query("SELECT c FROM Car c WHERE c.model.brand.id = :brandId")
    List<Car> findByBrandId(@Param("brandId") Long brandId);
    
    @Query("SELECT c FROM Car c WHERE c.modelYear = :year")
    List<Car> findByModelYear(@Param("year") Integer year);
    
    // Gelişmiş filtreleme sorguları
    @Query("SELECT c FROM Car c WHERE c.segment = :segment")
    List<Car> findBySegment(@Param("segment") String segment);
    
    @Query("SELECT c FROM Car c WHERE c.transmission = :transmission")
    List<Car> findByTransmission(@Param("transmission") String transmission);
    
    @Query("SELECT c FROM Car c WHERE c.fuelType = :fuelType")
    List<Car> findByFuelType(@Param("fuelType") String fuelType);
    
    @Query("SELECT c FROM Car c WHERE c.state = :state AND c.segment = :segment")
    List<Car> findByStateAndSegment(@Param("state") Integer state, @Param("segment") String segment);
    
    @Query("SELECT c FROM Car c WHERE LOWER(c.plate) LIKE LOWER(CONCAT('%', :plate, '%')) OR " +
           "LOWER(c.model.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.model.brand.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Car> findByPlateOrModelNameOrBrandName(@Param("plate") String plate, @Param("searchTerm") String searchTerm);
    
    // Özellik filtreleme
    @Query("SELECT DISTINCT c FROM Car c JOIN c.features f WHERE f = :feature")
    List<Car> findByFeature(@Param("feature") String feature);
    
    // Bakım tarihi yaklaşan araçlar
    @Query(value = "SELECT c.* FROM cars c WHERE c.next_maintenance_date <= CURRENT_DATE + INTERVAL '7 days'", nativeQuery = true)
    List<Car> findCarsWithUpcomingMaintenance();
    
    // Sigortası yakında dolacak araçlar
    @Query(value = "SELECT c.* FROM cars c WHERE c.insurance_expiry_date <= CURRENT_DATE + INTERVAL '30 days'", nativeQuery = true)
    List<Car> findCarsWithExpiringInsurance();
    
    // Bakım geçmişi ile birlikte araç getirme
    @Query("SELECT DISTINCT c FROM Car c LEFT JOIN FETCH c.maintenanceHistory WHERE c.id = :id")
    Optional<Car> findByIdWithMaintenanceHistory(@Param("id") Long id);
}

