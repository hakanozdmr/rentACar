package hakan.rentacar.entities.concretes;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime readAt;

    @Column
    private String relatedEntityType; // RESERVATION, RENTAL, etc.

    @Column
    private Long relatedEntityId;

    public enum NotificationType {
        RESERVATION_CONFIRMED,    // Rezervasyon onaylandı
        RESERVATION_REMINDER,     // Rezervasyon hatırlatması
        RESERVATION_CANCELLED,    // Rezervasyon iptal edildi
        CAR_DELIVERY,            // Araç teslim
        CAR_PICKUP,              // Araç teslim alma
        PAYMENT_DUE,             // Ödeme tarihi yaklaştı
        RENTAL_EXPIRED,          // Kiralama süresi doldu
        PROMOTION,               // Promosyon
        MAINTENANCE_REMINDER     // Bakım hatırlatması
    }

    public enum NotificationStatus {
        PENDING,    // Beklemede
        SENT,       // Gönderildi
        DELIVERED,  // Ulaştı
        READ,       // Okundu
        FAILED      // Başarısız
    }

    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        IN_APP     // Uygulama içi bildirim
    }
}





