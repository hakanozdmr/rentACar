package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.VehicleConditionCheck;
import hakan.rentacar.entities.concretes.VehicleConditionCheck.CheckType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleConditionCheckRepository extends JpaRepository<VehicleConditionCheck, Long> {
    
    List<VehicleConditionCheck> findByRentalId(Long rentalId);
    
    List<VehicleConditionCheck> findByCarId(Long carId);
    
    List<VehicleConditionCheck> findByCheckType(CheckType checkType);
    
    List<VehicleConditionCheck> findByRentalIdAndCheckType(Long rentalId, CheckType checkType);
    
    @Query("SELECT vcc FROM VehicleConditionCheck vcc WHERE vcc.rental.id = :rentalId ORDER BY vcc.performedAt DESC")
    List<VehicleConditionCheck> findByRentalIdOrderByPerformedAtDesc(@Param("rentalId") Long rentalId);
    
    @Query("SELECT vcc FROM VehicleConditionCheck vcc WHERE vcc.performedAt BETWEEN :startDate AND :endDate")
    List<VehicleConditionCheck> findChecksBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT vcc FROM VehicleConditionCheck vcc WHERE vcc.car.id = :carId AND vcc.checkType = :checkType ORDER BY vcc.performedAt DESC")
    List<VehicleConditionCheck> findByCarIdAndCheckTypeOrderByPerformedAtDesc(@Param("carId") Long carId, @Param("checkType") CheckType checkType);
    
    @Query("SELECT vcc FROM VehicleConditionCheck vcc WHERE vcc.rental.id = :rentalId AND vcc.checkType = :checkType ORDER BY vcc.performedAt DESC LIMIT 1")
    Optional<VehicleConditionCheck> findLatestByRentalIdAndCheckType(@Param("rentalId") Long rentalId, @Param("checkType") CheckType checkType);
    
    @Query("SELECT COUNT(vcc) FROM VehicleConditionCheck vcc WHERE vcc.needsMaintenance = true AND vcc.isConfirmed = false")
    Long countPendingMaintenanceChecks();
}


