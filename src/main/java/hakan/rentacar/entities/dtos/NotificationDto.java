package hakan.rentacar.entities.dtos;

import hakan.rentacar.entities.concretes.Notification;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    
    @NotNull
    private Long customerId;
    
    private String customerName;
    
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    @Size(max = 1000)
    private String message;
    
    private Notification.NotificationType type;
    
    private Notification.NotificationStatus status;
    
    private Notification.NotificationChannel channel;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime readAt;
    
    private String relatedEntityType;
    
    private Long relatedEntityId;
    
    private LocalDateTime createdAt;
}





