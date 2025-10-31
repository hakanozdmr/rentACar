package hakan.rentacar.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hakan.rentacar.entities.concretes.AuditLog;
import hakan.rentacar.entities.dtos.AuditLogDto;
import hakan.rentacar.entities.dtos.AuditStatisticsDto;
import hakan.rentacar.repostories.AuditLogRepository;
import hakan.rentacar.service.AuditLogService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Helper method to map AuditLog to AuditLogDto
     */
    private AuditLogDto mapToDto(AuditLog auditLog) {
        return modelMapper.map(auditLog, AuditLogDto.class);
    }

    @Override
    public AuditLogDto create(AuditLogDto auditLogDto) {
        AuditLog auditLog = modelMapper.map(auditLogDto, AuditLog.class);
        
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        AuditLog savedAuditLog = auditLogRepository.save(auditLog);
        
        return mapToDto(savedAuditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findByOrderByTimestampDesc(pageable);
        return auditLogs.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByEntityName(String entityName) {
        List<AuditLog> auditLogs = auditLogRepository.findByEntityNameOrderByTimestampDesc(entityName);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByEntityNameAndId(String entityName, Long entityId) {
        List<AuditLog> auditLogs = auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByUserId(Long userId) {
        List<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByUsername(String username) {
        List<AuditLog> auditLogs = auditLogRepository.findByUsernameOrderByTimestampDesc(username);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByActionType(AuditLog.ActionType actionType) {
        List<AuditLog> auditLogs = auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogsWithFilters(String entityName, AuditLog.ActionType actionType, 
                                                     Long userId, LocalDateTime startDate, LocalDateTime endDate, 
                                                     Pageable pageable) {
        // Use Specification for dynamic filtering
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (entityName != null && !entityName.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("entityName"), entityName));
            }
            
            if (actionType != null) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }
            
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<AuditLog> auditLogs = auditLogRepository.findAll(spec, pageable);
        return auditLogs.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getEntityHistory(String entityName, Long entityId) {
        List<AuditLog> auditLogs = auditLogRepository.findEntityHistory(entityName, entityId);
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentAuditLogs(int limit) {
        List<AuditLog> auditLogs = auditLogRepository.findTop100ByOrderByTimestampDesc();
        return auditLogs.stream()
                .limit(limit)
                .map(auditLog -> modelMapper.map(auditLog, AuditLogDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getFailedOperations() {
        List<AuditLog> auditLogs = auditLogRepository.findByOperationResultOrderByTimestampDesc("FAILURE");
        auditLogs.addAll(auditLogRepository.findByOperationResultOrderByTimestampDesc("ERROR"));
        return auditLogs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteOldAuditLogs(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditStatisticsDto getAuditStatistics() {
        // This would need custom queries for statistics
        // For now, returning basic implementation
        return AuditStatisticsDto.builder()
                .totalAuditLogs(auditLogRepository.count())
                .build();
    }

    // Helper method to serialize object to JSON string
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    // Helper method to find changed fields between two objects
    public String findChangedFields(Object oldObject, Object newObject) {
        // This would need custom implementation based on your needs
        // Could use reflection or libraries like Apache Commons BeanUtils
        return "";
    }
}
