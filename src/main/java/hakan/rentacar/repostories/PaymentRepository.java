package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByCustomerId(Long customerId);
    
    List<Payment> findByRentalId(Long rentalId);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.createdDate DESC")
    List<Payment> findByCustomerIdOrderByCreatedDateDesc(@Param("customerId") Long customerId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.dueDate < :currentDate ORDER BY p.dueDate ASC")
    List<Payment> findOverduePayments(@Param("status") Payment.PaymentStatus status, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT p FROM Payment p WHERE p.dueDate BETWEEN :startDate AND :endDate ORDER BY p.dueDate ASC")
    List<Payment> findPaymentsDueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.rental.id = :rentalId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidAmountForRental(@Param("rentalId") Long rentalId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.rental.id = :rentalId")
    Long getPaymentCountForRental(@Param("rentalId") Long rentalId);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.rental.id = :rentalId AND p.method = :method AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByRentalAndMethod(@Param("rentalId") Long rentalId, @Param("method") Payment.PaymentMethod method);
    
    // Financial reporting queries
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p.method, SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt BETWEEN :startDate AND :endDate GROUP BY p.method")
    List<Object[]> getRevenueByMethodBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}




