package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    
    List<MaintenanceRecord> findByCarIdOrderByMaintenanceDateDesc(Long carId);
    
    List<MaintenanceRecord> findByCarIdAndMaintenanceDateBetween(Long carId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.car.id = :carId AND mr.type = :type ORDER BY mr.maintenanceDate DESC")
    List<MaintenanceRecord> findByCarIdAndTypeOrderByMaintenanceDateDesc(@Param("carId") Long carId, @Param("type") String type);
    
    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.car.id = :carId")
    Long countByCarId(@Param("carId") Long carId);
}
