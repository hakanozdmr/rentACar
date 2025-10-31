package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_by", updatable = false)
    @CreatedBy
    private String createdBy;

    @Column(name = "created_date", updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "update_by")
    @LastModifiedBy
    private String updateBy;

    @Column(name = "update_date")
    @LastModifiedDate
    private LocalDateTime updateDate;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_url", length = 1000)
    private String requestUrl;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "changed_fields", length = 2000)
    private String changedFields;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "operation_result", length = 50)
    private String operationResult; // SUCCESS, FAILURE, ERROR

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum ActionType {
        CREATE("Oluşturuldu"),
        UPDATE("Güncellendi"),
        DELETE("Silindi"),
        READ("Okundu"),
        LOGIN("Giriş"),
        LOGOUT("Çıkış"),
        ACCESS_DENIED("Erişim Reddedildi"),
        EXPORT("Dışa Aktarıldı"),
        IMPORT("İçe Aktarıldı"),
        BACKUP("Yedekleme"),
        RESTORE("Geri Yükleme"),
        BULK_UPDATE("Toplu Güncelleme"),
        BULK_DELETE("Toplu Silme");

        private final String displayName;

        ActionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

