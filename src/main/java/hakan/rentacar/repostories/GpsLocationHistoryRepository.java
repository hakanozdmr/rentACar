package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.GpsLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GpsLocationHistoryRepository extends JpaRepository<GpsLocationHistory, Long> {
    
    /**
     * Belirli araç için son konumları getirir
     */
    List<GpsLocationHistory> findByCarIdOrderByRecordedAtDesc(Long carId);
    
    /**
     * Belirli araç için belirli tarih aralığındaki konumları getirir
     */
    List<GpsLocationHistory> findByCarIdAndRecordedAtBetweenOrderByRecordedAtDesc(
        Long carId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Tüm araçların son konumlarını getirir
     */
    @Query(value = """
        SELECT DISTINCT ON (g.car_id) g.*
        FROM gps_location_history g
        ORDER BY g.car_id, g.recorded_at DESC
        """, nativeQuery = true)
    List<GpsLocationHistory> findLatestLocationForAllCars();
    
    /**
     * Belirli yarıçap içindeki araç konumlarını getirir
     */
    @Query(value = """
        SELECT h.*
        FROM gps_location_history h
        INNER JOIN (
            SELECT car_id, MAX(recorded_at) as max_time
            FROM gps_location_history
            WHERE recorded_at > :since
            GROUP BY car_id
        ) latest ON h.car_id = latest.car_id AND h.recorded_at = latest.max_time
        WHERE (
            6371 * acos(
                cos(radians(:centerLat)) * 
                cos(radians(h.latitude)) * 
                cos(radians(h.longitude) - radians(:centerLng)) + 
                sin(radians(:centerLat)) * 
                sin(radians(h.latitude))
            )
        ) <= :radiusKm
        """, nativeQuery = true)
    List<GpsLocationHistory> findCarsWithinRadius(
        @Param("centerLat") Double centerLat,
        @Param("centerLng") Double centerLng,
        @Param("radiusKm") Double radiusKm,
        @Param("since") LocalDateTime since);
    
    /**
     * Belirli süre içindeki aktif araçları getirir
     */
    @Query(value = """
        SELECT h.*
        FROM gps_location_history h
        INNER JOIN (
            SELECT car_id, MAX(recorded_at) as max_time
            FROM gps_location_history
            GROUP BY car_id
        ) latest ON h.car_id = latest.car_id AND h.recorded_at = latest.max_time
        WHERE h.recorded_at > :since AND h.is_online = true
        """, nativeQuery = true)
    List<GpsLocationHistory> findRecentlyActiveCars(@Param("since") LocalDateTime since);
}





