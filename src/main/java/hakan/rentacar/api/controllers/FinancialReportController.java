package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.GeneralLedgerDto;
import hakan.rentacar.entities.dtos.PaymentDto;
import hakan.rentacar.entities.dtos.InvoiceDto;
import hakan.rentacar.service.GeneralLedgerService;
import hakan.rentacar.service.PaymentService;
import hakan.rentacar.service.InvoiceService;
import hakan.rentacar.service.TaxCalculationService;
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
@RequestMapping("/api/financial-reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Financial Reports", description = "Mali raporlama işlemleri")
public class FinancialReportController {

    @Autowired
    private GeneralLedgerService generalLedgerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private TaxCalculationService taxCalculationService;

    // Dashboard Summary
    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dashboard özeti", description = "Mali dashboard için özet bilgileri getirir")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        Map<String, Object> summary = new java.util.HashMap<>();
        
        // Revenue summary
        BigDecimal totalRevenue = paymentService.getTotalRevenue(start, end) != null ? paymentService.getTotalRevenue(start, end) : BigDecimal.ZERO;
        BigDecimal totalInvoiced = invoiceService.getTotalInvoicedAmount(start, end) != null ? invoiceService.getTotalInvoicedAmount(start, end) : BigDecimal.ZERO;
        BigDecimal totalPaid = invoiceService.getTotalPaidAmount(start, end) != null ? invoiceService.getTotalPaidAmount(start, end) : BigDecimal.ZERO;
        
        // Payment summary
        List<PaymentDto> pendingPayments = paymentService.getPendingPayments();
        List<PaymentDto> overduePayments = paymentService.getOverduePayments();
        List<PaymentDto> completedPayments = paymentService.getCompleted();
        
        // Calculate payment amounts
        BigDecimal pendingPaymentsAmount = pendingPayments.stream()
                .map(PaymentDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overduePaymentsAmount = overduePayments.stream()
                .map(PaymentDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Invoice summary
        List<InvoiceDto> pendingInvoices = invoiceService.getPendingInvoices();
        List<InvoiceDto> overdueInvoices = invoiceService.getOverdueInvoices();
        
        // Calculate invoice amounts
        BigDecimal pendingInvoicesAmount = pendingInvoices.stream()
                .map(InvoiceDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overdueInvoicesAmount = overdueInvoices.stream()
                .map(InvoiceDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Tax summary
        Map<String, Object> taxReport = taxCalculationService.getVATReport(start, end);
        BigDecimal totalTaxLiability = BigDecimal.ZERO;
        if (taxReport != null && taxReport.containsKey("totalTaxAmount")) {
            Object taxAmount = taxReport.get("totalTaxAmount");
            if (taxAmount instanceof BigDecimal) {
                totalTaxLiability = (BigDecimal) taxAmount;
            }
        }
        
        // Calculate net profit
        BigDecimal netProfit = totalRevenue.subtract(totalInvoiced.subtract(totalPaid));
        
        summary.put("periodStart", start);
        summary.put("periodEnd", end);
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalInvoiced", totalInvoiced);
        summary.put("totalPaid", totalPaid);
        summary.put("netProfit", netProfit);
        summary.put("pendingPayments", pendingPaymentsAmount);
        summary.put("overduePayments", overduePaymentsAmount);
        summary.put("pendingPaymentsCount", pendingPayments.size());
        summary.put("overduePaymentsCount", overduePayments.size());
        summary.put("pendingInvoicesCount", pendingInvoices.size());
        summary.put("overdueInvoicesCount", overdueInvoices.size());
        summary.put("totalTaxLiability", totalTaxLiability);
        summary.put("taxReport", taxReport);
        
        return ResponseEntity.ok(summary);
    }

    // Trial Balance
    @GetMapping("/trial-balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mizan", description = "Genel muhasebe mizanını getirir")
    public ResponseEntity<List<GeneralLedgerDto>> getTrialBalance() {
        List<GeneralLedgerDto> trialBalance = generalLedgerService.getTrialBalance();
        return ResponseEntity.ok(trialBalance);
    }

    // Income Statement (P&L)
    @GetMapping("/income-statement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gelir Tablosu", description = "Belirtilen dönem için gelir tablosu getirir")
    public ResponseEntity<Map<String, Object>> getIncomeStatement(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        Map<String, Object> incomeStatement = generalLedgerService.getIncomeStatement(start, end);
        return ResponseEntity.ok(incomeStatement);
    }

    // Balance Sheet
    @GetMapping("/balance-sheet")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bilanço", description = "Belirtilen tarih için bilanço getirir")
    public ResponseEntity<Map<String, Object>> getBalanceSheet(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String date) {
        
        LocalDateTime balanceSheetDate = LocalDateTime.parse(date + "T23:59:59");
        Map<String, Object> balanceSheet = generalLedgerService.getBalanceSheet(balanceSheetDate);
        return ResponseEntity.ok(balanceSheet);
    }

    // Cash Flow Statement
    @GetMapping("/cash-flow")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Nakit Akış Tablosu", description = "Belirtilen dönem için nakit akış tablosu getirir")
    public ResponseEntity<Map<String, Object>> getCashFlowStatement(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        try {
            // Parse dates safely
            String startDateTime = startDate.contains("T") ? startDate : startDate + "T00:00:00";
            String endDateTime = endDate.contains("T") ? endDate : endDate + "T23:59:59";
            
            LocalDateTime start = LocalDateTime.parse(startDateTime);
            LocalDateTime end = LocalDateTime.parse(endDateTime);
            
            Map<String, Object> cashFlowStatement = generalLedgerService.getCashFlowStatement(start, end);
            return ResponseEntity.ok(cashFlowStatement);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Invalid date format or processing error: " + e.getMessage());
            errorResponse.put("startDate", startDate);
            errorResponse.put("endDate", endDate);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Tax Reports
    @GetMapping("/tax-report")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vergi Raporu", description = "Belirtilen dönem için vergi raporu getirir")
    public ResponseEntity<Map<String, Object>> getTaxReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        Map<String, Object> taxReport = taxCalculationService.getVATReport(start, end);
        return ResponseEntity.ok(taxReport);
    }

    @GetMapping("/tax-summary/{period}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vergi Özeti", description = "Belirtilen vergi dönemi için özet getirir")
    public ResponseEntity<Map<String, Object>> getTaxSummaryByPeriod(@PathVariable String period) {
        Map<String, Object> taxSummary = taxCalculationService.getTaxSummaryByPeriod(period);
        return ResponseEntity.ok(taxSummary);
    }

    // Account Reports
    @GetMapping("/account-transactions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hesap Hareketleri", description = "Belirli hesap için hareketleri getirir")
    public ResponseEntity<List<GeneralLedgerDto>> getAccountTransactions(
            @RequestParam String accountType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {
        
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        // Convert string to enum - you might want to add validation here
        hakan.rentacar.entities.concretes.GeneralLedger.AccountType accountTypeEnum = 
                hakan.rentacar.entities.concretes.GeneralLedger.AccountType.valueOf(accountType);
        
        List<GeneralLedgerDto> transactions = generalLedgerService.getAccountTransactions(
                accountTypeEnum, start, end);
        
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account-balance/{accountType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hesap Bakiyesi", description = "Belirli hesap için güncel bakiyeyi getirir")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable String accountType) {
        hakan.rentacar.entities.concretes.GeneralLedger.AccountType accountTypeEnum = 
                hakan.rentacar.entities.concretes.GeneralLedger.AccountType.valueOf(accountType);
        
        BigDecimal balance = generalLedgerService.getAccountBalance(accountTypeEnum);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("accountType", accountType);
        result.put("balance", balance);
        result.put("accountTypeDisplayName", accountTypeEnum.getDisplayName());
        
        return ResponseEntity.ok(result);
    }

    // Reconciliation
    @GetMapping("/unreconciled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mutabakat Bekleyen", description = "Mutabakat bekleyen işlemleri getirir")
    public ResponseEntity<List<GeneralLedgerDto>> getUnreconciledTransactions() {
        List<GeneralLedgerDto> unreconciled = generalLedgerService.getUnreconciledTransactions();
        return ResponseEntity.ok(unreconciled);
    }

    @PutMapping("/reconcile/{ledgerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mutabakat İşaretle", description = "Belirli işlemi mutabakatlı olarak işaretler")
    public ResponseEntity<String> markAsReconciled(@PathVariable Long ledgerId) {
        generalLedgerService.markAsReconciled(ledgerId);
        return ResponseEntity.ok("Transaction marked as reconciled");
    }
}
