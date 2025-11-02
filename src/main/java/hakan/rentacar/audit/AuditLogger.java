package hakan.rentacar.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hakan.rentacar.entities.concretes.AuditLog;
import hakan.rentacar.entities.dtos.AuditLogDto;
import hakan.rentacar.security.JwtUtils;
import hakan.rentacar.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ThreadLocal<AuditContext> auditContext = new ThreadLocal<>();

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        AuditContext context = new AuditContext();
        auditContext.set(context);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Before execution - capture request info
            captureRequestInfo(context, joinPoint, auditable);
            
            Object result = joinPoint.proceed();
            
            // After execution - log success
            long executionTime = System.currentTimeMillis() - startTime;
            context.setExecutionTime(executionTime);
            context.setResult("SUCCESS");
            
            logAuditEvent(context, auditable);
            
            return result;
            
        } catch (Exception e) {
            // On exception - log failure
            long executionTime = System.currentTimeMillis() - startTime;
            context.setExecutionTime(executionTime);
            context.setResult("FAILURE");
            context.setErrorMessage(e.getMessage());
            
            logAuditEvent(context, auditable);
            
            throw e;
        } finally {
            auditContext.remove();
        }
    }

    @Before("@annotation(auditable)")
    public void beforeMethod(JoinPoint joinPoint, Auditable auditable) {
        AuditContext context = auditContext.get();
        if (context != null) {
            captureMethodInfo(context, joinPoint, auditable);
        }
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        AuditContext context = auditContext.get();
        if (context != null) {
            context.setNewValues(toJson(result));
        }
    }

    private void captureRequestInfo(AuditContext context, JoinPoint joinPoint, Auditable auditable) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                context.setRequestMethod(request.getMethod());
                context.setRequestUrl(request.getRequestURL().toString());
                context.setIpAddress(getClientIpAddress(request));
                context.setUserAgent(request.getHeader("User-Agent"));
                context.setSessionId(request.getSession().getId());
            }

            // Get current user info
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                String username = authentication.getName();
                context.setUsername(username);
                // You might want to get more user details from UserDetails
            }

        } catch (Exception e) {
            logger.error("Error capturing request info for audit", e);
        }
    }

    private void captureMethodInfo(AuditContext context, JoinPoint joinPoint, Auditable auditable) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            context.setEntityName(auditable.entity());
            context.setActionType(determineActionType(methodName, auditable.action()));
            
            // Capture method arguments for CREATE and UPDATE operations
            if (joinPoint.getArgs().length > 0) {
                Object firstArg = joinPoint.getArgs()[0];
                if (firstArg != null) {
                    try {
                        Field idField = findIdField(firstArg);
                        if (idField != null && auditable.action() != AuditLog.ActionType.CREATE) {
                            idField.setAccessible(true);
                            Object idValue = idField.get(firstArg);
                            if (idValue != null) {
                                context.setEntityId(Long.valueOf(idValue.toString()));
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Could not extract entity ID", e);
                    }
                }
                
                if (auditable.action() == AuditLog.ActionType.CREATE || 
                    auditable.action() == AuditLog.ActionType.UPDATE) {
                    context.setNewValues(toJson(firstArg));
                }
            }

        } catch (Exception e) {
            logger.error("Error capturing method info for audit", e);
        }
    }

    private AuditLog.ActionType determineActionType(String methodName, AuditLog.ActionType annotationValue) {
        if (annotationValue != AuditLog.ActionType.READ) {
            return annotationValue;
        }
        
        // Auto-detect action type from method name
        String lowerMethodName = methodName.toLowerCase();
        if (lowerMethodName.startsWith("create") || lowerMethodName.startsWith("add") || 
            lowerMethodName.startsWith("save") && !lowerMethodName.contains("update")) {
            return AuditLog.ActionType.CREATE;
        } else if (lowerMethodName.startsWith("update") || lowerMethodName.startsWith("edit") || 
                   lowerMethodName.contains("update")) {
            return AuditLog.ActionType.UPDATE;
        } else if (lowerMethodName.startsWith("delete") || lowerMethodName.startsWith("remove")) {
            return AuditLog.ActionType.DELETE;
        } else {
            return AuditLog.ActionType.READ;
        }
    }

    private Field findIdField(Object obj) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals("id")) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private void logAuditEvent(AuditContext context, Auditable auditable) {
        try {
            AuditLogDto auditLogDto = AuditLogDto.builder()
                    .entityName(context.getEntityName())
                    .entityId(context.getEntityId())
                    .actionType(context.getActionType())
                    .userId(context.getUserId())
                    .username(context.getUsername())
                    .userEmail(context.getUserEmail())
                    .ipAddress(context.getIpAddress())
                    .userAgent(context.getUserAgent())
                    .requestMethod(context.getRequestMethod())
                    .requestUrl(context.getRequestUrl())
                    .oldValues(context.getOldValues())
                    .newValues(context.getNewValues())
                    .changedFields(context.getChangedFields())
                    .sessionId(context.getSessionId())
                    .operationResult(context.getResult())
                    .errorMessage(context.getErrorMessage())
                    .executionTimeMs(context.getExecutionTime())
                    .timestamp(LocalDateTime.now())
                    .additionalInfo(context.getAdditionalInfo())
                    .build();

            auditLogService.create(auditLogDto);
            
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    // Inner class for audit context
    private static class AuditContext {
        private String entityName;
        private Long entityId;
        private AuditLog.ActionType actionType;
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
        private String result;
        private String errorMessage;
        private Long executionTime;
        private String additionalInfo;

        // Getters and setters
        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        
        public AuditLog.ActionType getActionType() { return actionType; }
        public void setActionType(AuditLog.ActionType actionType) { this.actionType = actionType; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public String getRequestMethod() { return requestMethod; }
        public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
        
        public String getRequestUrl() { return requestUrl; }
        public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
        
        public String getOldValues() { return oldValues; }
        public void setOldValues(String oldValues) { this.oldValues = oldValues; }
        
        public String getNewValues() { return newValues; }
        public void setNewValues(String newValues) { this.newValues = newValues; }
        
        public String getChangedFields() { return changedFields; }
        public void setChangedFields(String changedFields) { this.changedFields = changedFields; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Long getExecutionTime() { return executionTime; }
        public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }
        
        public String getAdditionalInfo() { return additionalInfo; }
        public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
    }
}




