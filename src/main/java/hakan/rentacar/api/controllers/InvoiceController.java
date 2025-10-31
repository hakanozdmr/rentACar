package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.InvoiceDto;
import hakan.rentacar.entities.dtos.InvoiceSummaryDto;
import hakan.rentacar.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Invoice Management", description = "Fatura yönetimi işlemleri")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tüm faturaları listele", description = "Sistem yöneticileri için tüm faturaları listeler")
    public ResponseEntity<List<InvoiceDto>> getAllInvoices() {
        List<InvoiceDto> invoices = invoiceService.getAll();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fatura detayı", description = "Belirli bir faturanın detaylarını getirir")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Müşteri faturaları", description = "Belirli müşterinin faturalarını listeler")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByCustomer(@PathVariable Long customerId) {
        List<InvoiceDto> invoices = invoiceService.getByCustomerId(customerId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/rental/{rentalId}")
    @Operation(summary = "Kiralama faturaları", description = "Belirli kiralama için kesilen faturaları listeler")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByRental(@PathVariable Long rentalId) {
        List<InvoiceDto> invoices = invoiceService.getByRentalId(rentalId);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yeni fatura oluştur", description = "Yeni bir fatura kaydı oluşturur")
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto createdInvoice = invoiceService.create(invoiceDto);
        return ResponseEntity.ok(createdInvoice);
    }

    @PostMapping("/generate-for-rental/{rentalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kiralama için fatura oluştur", description = "Belirli kiralama için otomatik fatura oluşturur")
    public ResponseEntity<InvoiceDto> generateInvoiceForRental(@PathVariable Long rentalId) {
        InvoiceDto invoice = invoiceService.generateInvoiceForRental(rentalId);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fatura güncelle", description = "Mevcut fatura kaydını günceller")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceDto invoiceDto) {
        invoiceDto.setId(id);
        InvoiceDto updatedInvoice = invoiceService.update(invoiceDto);
        return ResponseEntity.ok(updatedInvoice);
    }

    @PutMapping("/{id}/mark-sent")
    @Operation(summary = "Faturayı gönderildi olarak işaretle", description = "Fatura gönderildiğini işaretler ve email gönderir")
    public ResponseEntity<InvoiceDto> markInvoiceAsSent(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.markAsSent(id);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}/mark-paid")
    @Operation(summary = "Faturayı ödendi olarak işaretle", description = "Fatura ödendiğini işaretler")
    public ResponseEntity<InvoiceDto> markInvoiceAsPaid(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(invoice);
    }

    @PutMapping("/{id}/mark-cancelled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Faturayı iptal olarak işaretle", description = "Fatura iptal edildiğini işaretler")
    public ResponseEntity<InvoiceDto> markInvoiceAsCancelled(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.markAsCancelled(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bekleyen faturalar", description = "Beklemede olan faturaları listeler")
    public ResponseEntity<List<InvoiceDto>> getPendingInvoices() {
        List<InvoiceDto> invoices = invoiceService.getPendingInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vadesi geçen faturalar", description = "Vadesi geçen faturaları listeler")
    public ResponseEntity<List<InvoiceDto>> getOverdueInvoices() {
        List<InvoiceDto> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fatura özeti", description = "Fatura istatistiklerini getirir")
    public ResponseEntity<InvoiceSummaryDto> getInvoiceSummary() {
        InvoiceSummaryDto summary = invoiceService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/total-amount")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toplam fatura tutarı", description = "Belirli tarih aralığındaki toplam fatura tutarını getirir")
    public ResponseEntity<BigDecimal> getTotalInvoicedAmount(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        BigDecimal totalAmount = invoiceService.getTotalInvoicedAmount(start, end);
        return ResponseEntity.ok(totalAmount);
    }

    @GetMapping("/due-between")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Belirli tarihlerde vadesi gelen faturalar", description = "Tarih aralığında vadesi gelen faturaları listeler")
    public ResponseEntity<List<InvoiceDto>> getInvoicesDueBetween(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<InvoiceDto> invoices = invoiceService.getInvoicesDueBetween(start, end);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gelir raporu", description = "Belirli tarih aralığındaki toplam geliri getirir")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        BigDecimal totalInvoiced = invoiceService.getTotalInvoicedAmount(start, end);
        BigDecimal totalPaid = invoiceService.getTotalPaidAmount(start, end);
        
        Map<String, Object> report = new java.util.HashMap<>();
        report.put("startDate", start);
        report.put("endDate", end);
        report.put("totalInvoiced", totalInvoiced);
        report.put("totalPaid", totalPaid);
        report.put("totalOutstanding", totalInvoiced.subtract(totalPaid));
        
        return ResponseEntity.ok(report);
    }

    @PutMapping("/update-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vadesi geçen faturaları güncelle", description = "Vadesi geçen faturaları otomatik olarak günceller")
    public ResponseEntity<String> updateOverdueInvoices() {
        invoiceService.updateOverdueInvoices();
        return ResponseEntity.ok("Overdue invoices updated successfully");
    }

    // Tax calculation endpoints
    @GetMapping("/{id}/tax-details")
    @Operation(summary = "Fatura vergi detayları", description = "Belirli fatura için vergi hesaplama detaylarını getirir")
    public ResponseEntity<Map<String, Object>> getInvoiceTaxDetails(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getById(id);
        
        Map<String, Object> taxDetails = new java.util.HashMap<>();
        taxDetails.put("subtotal", invoice.getSubtotal());
        taxDetails.put("taxRate", invoice.getTaxRate());
        taxDetails.put("taxAmount", invoice.getTaxAmount());
        taxDetails.put("totalAmount", invoice.getTotalAmount());
        taxDetails.put("taxPercentage", invoice.getTaxRate() != null ? 
                invoice.getTaxRate().multiply(java.math.BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        
        return ResponseEntity.ok(taxDetails);
    }
}
