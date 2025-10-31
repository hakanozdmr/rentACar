package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /**
     * Find audit logs by entity name
     */
    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);

    /**
     * Find audit logs by entity name and entity ID
     */
    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);

    /**
     * Find audit logs by user ID
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find audit logs by username
     */
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByActionTypeOrderByTimestampDesc(AuditLog.ActionType actionType);

    /**
     * Find audit logs by date range
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find audit logs by IP address
     */
    List<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);

    /**
     * Find audit logs with pagination
     */
    Page<AuditLog> findByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find audit logs by entity name with pagination
     */
    Page<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName, Pageable pageable);

    /**
     * Find audit logs by user with pagination
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find audit logs with filters and pagination
     * We'll handle filtering logic in service layer for better flexibility
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Count audit logs by entity name
     */
    long countByEntityName(String entityName);

    /**
     * Count audit logs by action type
     */
    long countByActionType(AuditLog.ActionType actionType);

    /**
     * Count audit logs by user ID
     */
    long countByUserId(Long userId);

    /**
     * Get recent audit logs
     */
    @Query("SELECT al FROM AuditLog al ORDER BY al.timestamp DESC")
    List<AuditLog> findTop100ByOrderByTimestampDesc();

    /**
     * Find failed operations
     */
    List<AuditLog> findByOperationResultOrderByTimestampDesc(String operationResult);

    /**
     * Find audit logs for specific entity history
     */
    @Query("SELECT al FROM AuditLog al WHERE al.entityName = :entityName AND al.entityId = :entityId ORDER BY al.timestamp DESC")
    List<AuditLog> findEntityHistory(@Param("entityName") String entityName, @Param("entityId") Long entityId);

    /**
     * Delete old audit logs (for maintenance)
     */
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate")
    void deleteOldAuditLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}
