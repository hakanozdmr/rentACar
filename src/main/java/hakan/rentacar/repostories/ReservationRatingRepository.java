package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.ReservationRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRatingRepository extends JpaRepository<ReservationRating, Long> {
    
    Optional<ReservationRating> findByRentalId(Long rentalId);
    
    List<ReservationRating> findByCustomerIdOrderByCreatedDateDesc(Long customerId);
    
    @Query("SELECT r FROM ReservationRating r WHERE r.carRating = true AND r.isPublic = true ORDER BY r.createdDate DESC")
    List<ReservationRating> findPublicCarRatings();
    
    @Query("SELECT r FROM ReservationRating r WHERE r.carRating = true AND r.isPublic = true AND r.rental.car.id = :carId ORDER BY r.createdDate DESC")
    List<ReservationRating> findPublicCarRatingsByCarId(@Param("carId") Long carId);
    
    @Query("SELECT AVG(r.rating) FROM ReservationRating r WHERE r.rental.car.id = :carId AND r.carRating = true")
    Double getAverageCarRating(@Param("carId") Long carId);
    
    @Query("SELECT COUNT(r) FROM ReservationRating r WHERE r.rental.car.id = :carId AND r.carRating = true")
    Long getCarRatingCount(@Param("carId") Long carId);
    
    // Check if customer has rated a rental
    @Query("SELECT COUNT(r) > 0 FROM ReservationRating r WHERE r.customer.id = :customerId AND r.rental.car.id = :carId")
    Boolean hasCustomerRatedRental(@Param("customerId") Long customerId, @Param("carId") Long carId);
    
    // Find rating by customer and car for completed rental
    @Query("SELECT r FROM ReservationRating r WHERE r.customer.id = :customerId AND r.rental.car.id = :carId")
    Optional<ReservationRating> findByCustomerIdAndCarIdForCompletedRental(@Param("customerId") Long customerId, @Param("carId") Long carId);
}

