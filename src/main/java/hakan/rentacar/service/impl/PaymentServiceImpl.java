package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.dtos.PaymentDto;
import hakan.rentacar.entities.dtos.PaymentSummaryDto;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.repostories.PaymentRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.service.EmailService;
import hakan.rentacar.service.PaymentService;
import hakan.rentacar.audit.Auditable;
import hakan.rentacar.entities.concretes.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    @Auditable(entity = "Payment", action = AuditLog.ActionType.CREATE, description = "Create new payment")
    public PaymentDto create(PaymentDto paymentDto) {
        Payment payment = mapToEntity(paymentDto);
        Payment savedPayment = paymentRepository.save(payment);
        return mapToDto(savedPayment);
    }

    @Override
    @Transactional
    @Auditable(entity = "Payment", action = AuditLog.ActionType.UPDATE, description = "Update payment")
    public PaymentDto update(PaymentDto paymentDto) {
        Payment existingPayment = paymentRepository.findById(paymentDto.getId()).orElseThrow();
        
        // Update fields
        existingPayment.setAmount(paymentDto.getAmount());
        existingPayment.setMethod(paymentDto.getMethod());
        existingPayment.setStatus(paymentDto.getStatus());
        existingPayment.setNotes(paymentDto.getNotes());
        existingPayment.setDueDate(paymentDto.getDueDate());
        
        if (paymentDto.getTransactionId() != null) {
            existingPayment.setTransactionId(paymentDto.getTransactionId());
        }
        if (paymentDto.getPaymentReference() != null) {
            existingPayment.setPaymentReference(paymentDto.getPaymentReference());
        }

        Payment savedPayment = paymentRepository.save(existingPayment);
        return mapToDto(savedPayment);
    }

    @Override
    public PaymentDto getById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow();
        return mapToDto(payment);
    }

    @Override
    public List<PaymentDto> getAll() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Auditable(entity = "Payment", action = AuditLog.ActionType.DELETE, description = "Delete payment")
    public void delete(Long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public List<PaymentDto> getByCustomerId(Long customerId) {
        List<Payment> payments = paymentRepository.findByCustomerIdOrderByCreatedDateDesc(customerId);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getByRentalId(Long rentalId) {
        List<Payment> payments = paymentRepository.findByRentalId(rentalId);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getPendingPayments() {
        List<Payment> payments = paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDto> getOverduePayments() {
        List<Payment> payments = paymentRepository.findOverduePayments(Payment.PaymentStatus.PENDING, LocalDateTime.now());
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentDto markAsPaid(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Send confirmation email
        try {
            emailService.sendPaymentConfirmation(payment.getCustomer(), payment);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Error sending payment confirmation email: " + e.getMessage());
        }
        
        return mapToDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto markAsFailed(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        payment.setStatus(Payment.PaymentStatus.FAILED);
        if (reason != null) {
            payment.setNotes(payment.getNotes() + "\nPayment failed: " + reason);
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        return mapToDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto processRefund(Long paymentId, BigDecimal refundAmount) {
        Payment originalPayment = paymentRepository.findById(paymentId).orElseThrow();
        
        if (!originalPayment.getStatus().equals(Payment.PaymentStatus.COMPLETED)) {
            throw new IllegalArgumentException("Can only refund completed payments");
        }
        
        if (refundAmount.compareTo(originalPayment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed original payment amount");
        }
        
        // Create a refund payment record
        Payment refundPayment = Payment.builder()
                .rental(originalPayment.getRental())
                .customer(originalPayment.getCustomer())
                .amount(refundAmount.negate()) // Negative amount for refund
                .method(originalPayment.getMethod())
                .status(Payment.PaymentStatus.REFUNDED)
                .paidAt(LocalDateTime.now())
                .notes("Refund for payment ID: " + originalPayment.getId())
                .build();
        
        Payment savedRefund = paymentRepository.save(refundPayment);
        return mapToDto(savedRefund);
    }

    @Override
    public BigDecimal getTotalPaidForRental(Long rentalId) {
        BigDecimal totalPaid = paymentRepository.getTotalPaidAmountForRental(rentalId);
        return totalPaid != null ? totalPaid : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRemainingBalanceForRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        BigDecimal totalAmount = rental.getTotalAmount() != null ? rental.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal totalPaid = getTotalPaidForRental(rentalId);
        return totalAmount.subtract(totalPaid);
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalRevenue = paymentRepository.getTotalRevenueBetween(startDate, endDate);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    @Override
    public List<PaymentDto> getPaymentsDueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findPaymentsDueBetween(startDate, endDate);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendPaymentReminders() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);
        
        List<Payment> upcomingPayments = paymentRepository.findPaymentsDueBetween(tomorrow, nextWeek);
        
        for (Payment payment : upcomingPayments) {
            if (payment.getStatus().equals(Payment.PaymentStatus.PENDING)) {
                try {
                    emailService.sendPaymentReminder(payment.getCustomer(), payment);
                } catch (Exception e) {
                    System.err.println("Error sending payment reminder for payment ID " + payment.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    private PaymentDto mapToDto(Payment payment) {
        PaymentDto dto = modelMapper.map(payment, PaymentDto.class);
        
        // Set relationship IDs
        if (payment.getCustomer() != null) {
            dto.setCustomerId(payment.getCustomer().getId());
            dto.setCustomerName(payment.getCustomer().getFirstName() + " " + payment.getCustomer().getLastName());
            dto.setCustomerEmail(payment.getCustomer().getEmail());
        }
        
        if (payment.getRental() != null) {
            dto.setRentalId(payment.getRental().getId());
            if (payment.getRental().getCar() != null) {
                dto.setCarPlate(payment.getRental().getCar().getPlate());
                if (payment.getRental().getCar().getModel() != null) {
                    dto.setCarModelName(payment.getRental().getCar().getModel().getName());
                    if (payment.getRental().getCar().getModel().getBrand() != null) {
                        dto.setCarBrandName(payment.getRental().getCar().getModel().getBrand().getName());
                    }
                }
            }
        }
        
        // Set display names
        dto.setMethodDisplayName(payment.getMethod().getDisplayName());
        dto.setStatusDisplayName(payment.getStatus().getDisplayName());
        
        // Calculate due date info
        if (payment.getDueDate() != null) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), payment.getDueDate());
            dto.setDaysUntilDue(daysUntilDue);
            dto.setIsOverdue(daysUntilDue < 0 && payment.getStatus().equals(Payment.PaymentStatus.PENDING));
        }
        
        return dto;
    }

    private Payment mapToEntity(PaymentDto dto) {
        Payment payment = modelMapper.map(dto, Payment.class);
        
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow();
            payment.setCustomer(customer);
        }
        
        if (dto.getRentalId() != null) {
            Rental rental = rentalRepository.findById(dto.getRentalId()).orElseThrow();
            payment.setRental(rental);
        }
        
        return payment;
    }

    @Override
    public List<PaymentDto> getCompleted() {
        List<Payment> payments = paymentRepository.findByStatus(Payment.PaymentStatus.COMPLETED);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto markAsCompleted(Long paymentId) {
        return markAsPaid(paymentId, null);
    }

    @Override
    public PaymentSummaryDto getSummary() {
        List<Payment> allPayments = paymentRepository.findAll();
        
        BigDecimal totalAmount = allPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal pendingAmount = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal completedAmount = allPayments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal overdueAmount = paymentRepository.findOverduePayments(Payment.PaymentStatus.PENDING, LocalDateTime.now())
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, PaymentSummaryDto.PaymentMethodSummary> byMethod = allPayments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMethod().name(),
                        Collectors.reducing(
                                new PaymentSummaryDto.PaymentMethodSummary(BigDecimal.ZERO, 0L),
                                p -> new PaymentSummaryDto.PaymentMethodSummary(p.getAmount(), 1L),
                                (a, b) -> new PaymentSummaryDto.PaymentMethodSummary(
                                        a.getAmount().add(b.getAmount()),
                                        a.getCount() + b.getCount()
                                )
                        )
                ));
        
        return PaymentSummaryDto.builder()
                .totalAmount(totalAmount)
                .count((long) allPayments.size())
                .pendingAmount(pendingAmount)
                .completedAmount(completedAmount)
                .overdueAmount(overdueAmount)
                .byMethod(byMethod)
                .build();
    }
}
