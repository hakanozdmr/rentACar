package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByCustomerId(Long customerId);
    
    List<Invoice> findByRentalId(Long rentalId);
    
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.customer.id = :customerId ORDER BY i.createdDate DESC")
    List<Invoice> findByCustomerIdOrderByCreatedDateDesc(@Param("customerId") Long customerId);
    
    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.dueDate < :currentDate ORDER BY i.dueDate ASC")
    List<Invoice> findOverdueInvoices(@Param("status") Invoice.InvoiceStatus status, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate BETWEEN :startDate AND :endDate ORDER BY i.dueDate ASC")
    List<Invoice> findInvoicesDueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    @Query("SELECT i FROM Invoice i WHERE i.rental.id = :rentalId")
    List<Invoice> findByRentalIdOrderByCreatedDateDesc(@Param("rentalId") Long rentalId);
    
    // Financial reporting queries
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status IN ('SENT', 'PAID') AND i.issueDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalInvoicedAmountBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PAID' AND i.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaidAmountBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long getInvoiceCountByStatus(@Param("status") Invoice.InvoiceStatus status);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'OVERDUE'")
    BigDecimal getTotalOverdueAmount();
}




