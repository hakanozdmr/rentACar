package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long id;
    private String entityName;
    private Long entityId;
    private AuditLog.ActionType actionType;
    private String actionTypeDisplayName;
    private Long userId;
    private String username;
    private String userEmail;
    private String ipAddress;
    private String userAgent;
    private String requestMethod;
    private String requestUrl;
    private String oldValues;
    private String newValues;
    private String changedFields;
    private String sessionId;
    private String operationResult;
    private String errorMessage;
    private Long executionTimeMs;
    private LocalDateTime timestamp;
    private String additionalInfo;
    private String createdBy;
    private java.util.Date createdDate;
    private String updateBy;
    private java.util.Date updateDate;

    public String getActionTypeDisplayName() {
        return actionType != null ? actionType.getDisplayName() : null;
    }
}


