package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.Reservation;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;
import hakan.rentacar.entities.concretes.Contract;

public interface EmailService {
    void sendReservationConfirmation(Customer customer, Reservation reservation);
    void sendReservationNotification(Customer customer, Reservation reservation);
    void sendReservationReminder(Customer customer, Reservation reservation);
    void sendReservationCancellation(Customer customer, Reservation reservation);
    void sendCarDeliveryNotification(Customer customer, Rental rental);
    void sendCarPickupNotification(Customer customer, Rental rental);
    void sendPaymentReminder(Customer customer, Rental rental);
    void sendRatingRequest(Customer customer, Rental rental);
    
    // Payment-related email methods
    void sendPaymentConfirmation(Customer customer, Payment payment);
    void sendPaymentReminder(Customer customer, Payment payment);
    void sendPaymentDueNotification(Customer customer, Payment payment);
    
    // Invoice-related email methods
    void sendInvoiceNotification(Customer customer, Invoice invoice);
    void sendOverdueInvoiceNotification(Customer customer, Invoice invoice);
    
    // Contract-related email methods
    void sendContractEmail(Customer customer, Contract contract);
    void sendContractSignatureRequest(Customer customer, Contract contract);
    void sendContractSignedNotification(Customer customer, Contract contract);
    
    void sendCustomEmail(String to, String subject, String content);
}

