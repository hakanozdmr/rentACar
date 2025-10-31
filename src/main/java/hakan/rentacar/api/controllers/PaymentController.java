package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.PaymentDto;
import hakan.rentacar.entities.dtos.PaymentSummaryDto;
import hakan.rentacar.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Payment Management", description = "Ödeme yönetimi işlemleri")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tüm ödemeleri listele", description = "Sistem yöneticileri için tüm ödemeleri listeler")
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        List<PaymentDto> payments = paymentService.getAll();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ödeme detayı", description = "Belirli bir ödemenin detaylarını getirir")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        PaymentDto payment = paymentService.getById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Müşteri ödemeleri", description = "Belirli müşterinin ödemelerini listeler")
    public ResponseEntity<List<PaymentDto>> getPaymentsByCustomer(@PathVariable Long customerId) {
        List<PaymentDto> payments = paymentService.getByCustomerId(customerId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/rental/{rentalId}")
    @Operation(summary = "Kiralama ödemeleri", description = "Belirli kiralama için yapılan ödemeleri listeler")
    public ResponseEntity<List<PaymentDto>> getPaymentsByRental(@PathVariable Long rentalId) {
        List<PaymentDto> payments = paymentService.getByRentalId(rentalId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yeni ödeme oluştur", description = "Yeni bir ödeme kaydı oluşturur")
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody PaymentDto paymentDto) {
        PaymentDto createdPayment = paymentService.create(paymentDto);
        return ResponseEntity.ok(createdPayment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ödeme güncelle", description = "Mevcut ödeme kaydını günceller")
    public ResponseEntity<PaymentDto> updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentDto paymentDto) {
        paymentDto.setId(id);
        PaymentDto updatedPayment = paymentService.update(paymentDto);
        return ResponseEntity.ok(updatedPayment);
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ödemeyi tamamlandı olarak işaretle", description = "Ödeme tamamlandığını işaretler")
    public ResponseEntity<PaymentDto> markPaymentAsPaid(@PathVariable Long id, 
                                                       @RequestParam(required = false) String transactionId) {
        PaymentDto payment = paymentService.markAsPaid(id, transactionId);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}/mark-failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ödemeyi başarısız olarak işaretle", description = "Ödeme başarısız olduğunu işaretler")
    public ResponseEntity<PaymentDto> markPaymentAsFailed(@PathVariable Long id, 
                                                         @RequestParam(required = false) String reason) {
        PaymentDto payment = paymentService.markAsFailed(id, reason);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ödeme iadesi", description = "Tamamlanmış ödeme için iade işlemi yapar")
    public ResponseEntity<PaymentDto> processRefund(@PathVariable Long id, 
                                                   @RequestParam BigDecimal refundAmount) {
        PaymentDto refundPayment = paymentService.processRefund(id, refundAmount);
        return ResponseEntity.ok(refundPayment);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bekleyen ödemeler", description = "Beklemede olan ödemeleri listeler")
    public ResponseEntity<List<PaymentDto>> getPendingPayments() {
        List<PaymentDto> payments = paymentService.getPendingPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vadesi geçen ödemeler", description = "Vadesi geçen ödemeleri listeler")
    public ResponseEntity<List<PaymentDto>> getOverduePayments() {
        List<PaymentDto> payments = paymentService.getOverduePayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/completed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tamamlanan ödemeler", description = "Tamamlanan ödemeleri listeler")
    public ResponseEntity<List<PaymentDto>> getCompletedPayments() {
        List<PaymentDto> payments = paymentService.getCompleted();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ödeme özeti", description = "Ödeme istatistiklerini getirir")
    public ResponseEntity<PaymentSummaryDto> getPaymentSummary() {
        PaymentSummaryDto summary = paymentService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ödemeyi tamamlandı olarak işaretle", description = "Ödeme tamamlandığını işaretler")
    public ResponseEntity<PaymentDto> markPaymentAsCompleted(@PathVariable Long id) {
        PaymentDto payment = paymentService.markAsCompleted(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/rental/{rentalId}/balance")
    @Operation(summary = "Kiralama bakiye bilgisi", description = "Belirli kiralama için kalan bakiye bilgisini getirir")
    public ResponseEntity<BalanceResponse> getRentalBalance(@PathVariable Long rentalId) {
        BigDecimal totalPaid = paymentService.getTotalPaidForRental(rentalId);
        BigDecimal remainingBalance = paymentService.getRemainingBalanceForRental(rentalId);
        
        BalanceResponse response = new BalanceResponse();
        response.setTotalPaid(totalPaid);
        response.setRemainingBalance(remainingBalance);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gelir raporu", description = "Belirli tarih aralığındaki toplam geliri getirir")
    public ResponseEntity<RevenueResponse> getRevenue(@RequestParam String startDate, 
                                                     @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        
        BigDecimal totalRevenue = paymentService.getTotalRevenue(start, end);
        
        RevenueResponse response = new RevenueResponse();
        response.setStartDate(start);
        response.setEndDate(end);
        response.setTotalRevenue(totalRevenue);
        
        return ResponseEntity.ok(response);
    }

    // Response classes
    public static class BalanceResponse {
        private BigDecimal totalPaid;
        private BigDecimal remainingBalance;

        public BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public void setTotalPaid(BigDecimal totalPaid) {
            this.totalPaid = totalPaid;
        }

        public BigDecimal getRemainingBalance() {
            return remainingBalance;
        }

        public void setRemainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
        }
    }

    public static class RevenueResponse {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal totalRevenue;

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
    }
}
