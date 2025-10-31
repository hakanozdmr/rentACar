package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.NotificationDto;

import java.util.List;

public interface NotificationService {
    List<NotificationDto> getByCustomerId(Long customerId);
    List<NotificationDto> getUnreadByCustomerId(Long customerId);
    NotificationDto create(NotificationDto notificationDto);
    NotificationDto markAsRead(Long notificationId);
    void markAllAsRead(Long customerId);
    Long getUnreadCount(Long customerId);
    void sendReservationReminder(Long reservationId);
    void sendCarDeliveryNotification(Long rentalId);
    void sendCarPickupNotification(Long rentalId);
    void sendPaymentReminder(Long rentalId);
    void sendRatingRequest(Long rentalId);
    void processPendingNotifications();
}

