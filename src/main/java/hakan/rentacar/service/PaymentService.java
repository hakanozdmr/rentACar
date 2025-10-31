package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.PaymentDto;
import hakan.rentacar.entities.dtos.PaymentSummaryDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    
    // Basic CRUD operations
    PaymentDto create(PaymentDto paymentDto);
    PaymentDto update(PaymentDto paymentDto);
    PaymentDto getById(Long id);
    List<PaymentDto> getAll();
    void delete(Long id);
    
    // Payment-specific operations
    List<PaymentDto> getByCustomerId(Long customerId);
    List<PaymentDto> getByRentalId(Long rentalId);
    List<PaymentDto> getPendingPayments();
    List<PaymentDto> getOverduePayments();
    List<PaymentDto> getCompleted();
    
    // Payment processing
    PaymentDto markAsPaid(Long paymentId, String transactionId);
    PaymentDto markAsCompleted(Long paymentId);
    PaymentDto markAsFailed(Long paymentId, String reason);
    PaymentDto processRefund(Long paymentId, BigDecimal refundAmount);
    
    // Financial calculations
    BigDecimal getTotalPaidForRental(Long rentalId);
    BigDecimal getRemainingBalanceForRental(Long rentalId);
    BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate);
    
    // Due date management
    List<PaymentDto> getPaymentsDueBetween(LocalDateTime startDate, LocalDateTime endDate);
    void sendPaymentReminders();
    
    // Summary and reporting
    PaymentSummaryDto getSummary();
}
