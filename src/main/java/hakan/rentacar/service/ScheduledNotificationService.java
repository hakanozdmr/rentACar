package hakan.rentacar.service;

import org.springframework.stereotype.Service;

@Service
public interface ScheduledNotificationService {
    void processReservationReminders();
    void processPaymentReminders();
    void processCarPickupReminders();
    void processRatingRequests();
    void processAllNotifications();
}
