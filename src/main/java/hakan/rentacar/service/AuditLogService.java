package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.AuditLog;
import hakan.rentacar.entities.dtos.AuditLogDto;
import hakan.rentacar.entities.dtos.AuditStatisticsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    
    /**
     * Create a new audit log entry
     */
    AuditLogDto create(AuditLogDto auditLogDto);
    
    /**
     * Get all audit logs with pagination
     */
    Page<AuditLogDto> getAllAuditLogs(Pageable pageable);
    
    /**
     * Get audit logs by entity name
     */
    List<AuditLogDto> getByEntityName(String entityName);
    
    /**
     * Get audit logs by entity name and ID
     */
    List<AuditLogDto> getByEntityNameAndId(String entityName, Long entityId);
    
    /**
     * Get audit logs by user ID
     */
    List<AuditLogDto> getByUserId(Long userId);
    
    /**
     * Get audit logs by username
     */
    List<AuditLogDto> getByUsername(String username);
    
    /**
     * Get audit logs by action type
     */
    List<AuditLogDto> getByActionType(AuditLog.ActionType actionType);
    
    /**
     * Get audit logs by date range
     */
    List<AuditLogDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get audit logs with filters and pagination
     */
    Page<AuditLogDto> getAuditLogsWithFilters(
            String entityName,
            AuditLog.ActionType actionType,
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
    
    /**
     * Get entity change history
     */
    List<AuditLogDto> getEntityHistory(String entityName, Long entityId);
    
    /**
     * Get recent audit logs
     */
    List<AuditLogDto> getRecentAuditLogs(int limit);
    
    /**
     * Get failed operations
     */
    List<AuditLogDto> getFailedOperations();
    
    /**
     * Clean up old audit logs
     */
    void cleanupOldAuditLogs(int daysToKeep);
    
    /**
     * Get audit statistics
     */
    AuditStatisticsDto getAuditStatistics();
}


