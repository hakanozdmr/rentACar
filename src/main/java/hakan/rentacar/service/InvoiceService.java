package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.InvoiceDto;
import hakan.rentacar.entities.dtos.InvoiceSummaryDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceService {
    
    // Basic CRUD operations
    InvoiceDto create(InvoiceDto invoiceDto);
    InvoiceDto update(InvoiceDto invoiceDto);
    InvoiceDto getById(Long id);
    List<InvoiceDto> getAll();
    void delete(Long id);
    
    // Invoice-specific operations
    List<InvoiceDto> getByCustomerId(Long customerId);
    List<InvoiceDto> getByRentalId(Long rentalId);
    List<InvoiceDto> getPendingInvoices();
    List<InvoiceDto> getOverdueInvoices();
    
    // Invoice processing
    InvoiceDto markAsSent(Long invoiceId);
    InvoiceDto markAsPaid(Long invoiceId);
    InvoiceDto markAsCancelled(Long invoiceId);
    
    // Invoice generation
    InvoiceDto generateInvoiceForRental(Long rentalId);
    
    // Financial calculations
    BigDecimal getTotalInvoicedAmount(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalPaidAmount(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalOverdueAmount();
    
    // Due date management
    List<InvoiceDto> getInvoicesDueBetween(LocalDateTime startDate, LocalDateTime endDate);
    void updateOverdueInvoices();
    
    // Tax calculations
    BigDecimal calculateTaxAmount(BigDecimal subtotal, BigDecimal taxRate);
    BigDecimal calculateTotalWithTax(BigDecimal subtotal, BigDecimal taxRate);
    
    // Summary operations
    InvoiceSummaryDto getSummary();
}
