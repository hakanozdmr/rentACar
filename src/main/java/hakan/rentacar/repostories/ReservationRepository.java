package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByCustomerIdOrderByCreatedDateDesc(Long customerId);
    
    List<Reservation> findByCarIdOrderByCreatedDateDesc(Long carId);
    
    @Query("SELECT r FROM Reservation r WHERE r.customer.id = :customerId AND r.status = :status")
    List<Reservation> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                               @Param("status") Reservation.ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.car.id = :carId AND " +
           "((r.startDate <= :startDate AND r.endDate >= :startDate) OR " +
           "(r.startDate <= :endDate AND r.endDate >= :endDate) OR " +
           "(r.startDate >= :startDate AND r.endDate <= :endDate)) " +
           "AND r.status IN ('CONFIRMED', 'PENDING')")
    List<Reservation> findByCarIdAndDateRange(@Param("carId") Long carId, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.startDate <= :date")
    List<Reservation> findByStatusAndStartDateBefore(@Param("status") Reservation.ReservationStatus status, 
                                                    @Param("date") LocalDate date);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.startDate BETWEEN :startDate AND :endDate")
    List<Reservation> findPendingReservationsInDateRange(@Param("status") Reservation.ReservationStatus status,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    
    List<Reservation> findByStatusOrderByCreatedDateDesc(Reservation.ReservationStatus status);
    
    // Find reservations starting tomorrow (for reminder notifications)
    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.startDate = :tomorrow")
    List<Reservation> findConfirmedReservationsStartingTomorrow(@Param("tomorrow") LocalDate tomorrow);
    
    // Find reservation by customer, car and date range (to match with rental)
    @Query("SELECT r FROM Reservation r WHERE r.customer.id = :customerId AND r.car.id = :carId AND r.startDate = :startDate AND r.endDate = :endDate AND r.status IN ('CONFIRMED', 'COMPLETED')")
    Optional<Reservation> findByCustomerAndCarAndDates(@Param("customerId") Long customerId, 
                                                      @Param("carId") Long carId,
                                                      @Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);
    
    // Alternative: Find reservation by customer and car with overlapping dates (more flexible)
    @Query("SELECT r FROM Reservation r WHERE r.customer.id = :customerId AND r.car.id = :carId AND " +
           "((r.startDate <= :startDate AND r.endDate >= :startDate) OR " +
           "(r.startDate <= :endDate AND r.endDate >= :endDate) OR " +
           "(r.startDate >= :startDate AND r.endDate <= :endDate)) " +
           "AND r.status IN ('CONFIRMED', 'COMPLETED') ORDER BY r.createdDate DESC")
    List<Reservation> findByCustomerAndCarWithOverlappingDates(@Param("customerId") Long customerId, 
                                                              @Param("carId") Long carId,
                                                              @Param("startDate") LocalDate startDate, 
                                                              @Param("endDate") LocalDate endDate);
}
