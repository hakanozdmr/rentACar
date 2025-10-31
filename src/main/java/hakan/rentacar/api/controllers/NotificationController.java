package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.NotificationDto;
import hakan.rentacar.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Notifications", description = "Bildirim yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationDto>> getByCustomerId(@PathVariable Long customerId) {
        List<NotificationDto> notifications = notificationService.getByCustomerId(customerId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/customer/{customerId}/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadByCustomerId(@PathVariable Long customerId) {
        List<NotificationDto> notifications = notificationService.getUnreadByCustomerId(customerId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    public ResponseEntity<NotificationDto> create(@RequestBody NotificationDto notificationDto) {
        NotificationDto createdNotification = notificationService.create(notificationDto);
        return ResponseEntity.ok(createdNotification);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/customer/{customerId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long customerId) {
        notificationService.markAllAsRead(customerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/customer/{customerId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long customerId) {
        Long count = notificationService.getUnreadCount(customerId);
        return ResponseEntity.ok(count);
    }
}
