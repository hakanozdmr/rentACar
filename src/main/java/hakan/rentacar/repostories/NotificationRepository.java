package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByCustomerIdOrderByCreatedDateDesc(Long customerId);
    
    @Query("SELECT n FROM Notification n WHERE n.customer.id = :customerId AND n.status = :status ORDER BY n.createdDate DESC")
    List<Notification> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                               @Param("status") Notification.NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.customer.id = :customerId AND n.readAt IS NULL ORDER BY n.createdDate DESC")
    List<Notification> findUnreadByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.sentAt IS NULL")
    List<Notification> findPendingNotifications(@Param("status") Notification.NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.createdDate BETWEEN :startDate AND :endDate")
    List<Notification> findByTypeAndDateRange(@Param("type") Notification.NotificationType type,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customer.id = :customerId AND n.readAt IS NULL")
    Long countUnreadByCustomerId(@Param("customerId") Long customerId);
}



