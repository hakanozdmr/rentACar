package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Reservation;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.repostories.ReservationRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.service.ContractService;
import hakan.rentacar.service.NotificationService;
import hakan.rentacar.service.ScheduledNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduledNotificationServiceImpl implements ScheduledNotificationService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private ContractService contractService;

    @Override
    @Scheduled(cron = "0 0 9 * * *") // Run every day at 9 AM
    public void processReservationReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Reservation> reservationsStartingTomorrow = 
            reservationRepository.findConfirmedReservationsStartingTomorrow(tomorrow);
        
        for (Reservation reservation : reservationsStartingTomorrow) {
            try {
                notificationService.sendReservationReminder(reservation.getId());
                System.out.println("Reservation reminder sent for reservation ID: " + reservation.getId());
            } catch (Exception e) {
                System.err.println("Error sending reservation reminder for ID " + reservation.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 10 * * *") // Run every day at 10 AM
    public void processPaymentReminders() {
        LocalDate twoDaysFromNow = LocalDate.now().plusDays(2);
        List<Rental> rentalsEndingInTwoDays = 
            rentalRepository.findRentalsEndingInTwoDays(twoDaysFromNow);
        
        for (Rental rental : rentalsEndingInTwoDays) {
            try {
                notificationService.sendPaymentReminder(rental.getId());
                System.out.println("Payment reminder sent for rental ID: " + rental.getId());
            } catch (Exception e) {
                System.err.println("Error sending payment reminder for rental ID " + rental.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 11 * * *") // Run every day at 11 AM
    public void processCarPickupReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Rental> rentalsEndingTomorrow = 
            rentalRepository.findRentalsEndingTomorrow(tomorrow);
        
        for (Rental rental : rentalsEndingTomorrow) {
            try {
                notificationService.sendCarPickupNotification(rental.getId());
                System.out.println("Car pickup reminder sent for rental ID: " + rental.getId());
            } catch (Exception e) {
                System.err.println("Error sending car pickup reminder for rental ID " + rental.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 12 * * *") // Run every day at 12 PM
    public void processRatingRequests() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Rental> rentalsEndedYesterday = 
            rentalRepository.findRentalsEndedYesterday(yesterday);
        
        System.out.println("Scheduled task: Processing rating requests for " + rentalsEndedYesterday.size() + " rentals ended on " + yesterday);
        
        for (Rental rental : rentalsEndedYesterday) {
            try {
                notificationService.sendRatingRequest(rental.getId());
                System.out.println("Rating request sent for rental ID: " + rental.getId());
            } catch (Exception e) {
                System.err.println("Error sending rating request for rental ID " + rental.getId() + ": " + e.getMessage());
            }
        }
    }

    @Override
    @Scheduled(cron = "0 0 */6 * * *") // Run every 6 hours
    public void processAllNotifications() {
        try {
            notificationService.processPendingNotifications();
            System.out.println("Processed pending notifications");
        } catch (Exception e) {
            System.err.println("Error processing pending notifications: " + e.getMessage());
        }
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?") // Run every day at 1 AM
    public void processContractExpirations() {
        try {
            contractService.markAsExpired();
            System.out.println("Processed contract expirations");
        } catch (Exception e) {
            System.err.println("Error processing contract expirations: " + e.getMessage());
        }
    }
}
