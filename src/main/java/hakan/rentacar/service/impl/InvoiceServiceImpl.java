package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.Invoice;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.dtos.InvoiceDto;
import hakan.rentacar.entities.dtos.InvoiceSummaryDto;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.repostories.InvoiceRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.service.EmailService;
import hakan.rentacar.service.InvoiceService;
import hakan.rentacar.audit.Auditable;
import hakan.rentacar.entities.concretes.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

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
    @Auditable(entity = "Invoice", action = AuditLog.ActionType.CREATE, description = "Create new invoice")
    public InvoiceDto create(InvoiceDto invoiceDto) {
        Invoice invoice = mapToEntity(invoiceDto);
        
        // Generate invoice number if not provided
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }
        
        // Calculate tax if not provided
        if (invoice.getTaxAmount() == null && invoice.getTaxRate() != null) {
            invoice.setTaxAmount(calculateTaxAmount(invoice.getSubtotal(), invoice.getTaxRate()));
        }
        
        // Calculate total if not provided
        if (invoice.getTotalAmount() == null) {
            invoice.setTotalAmount(calculateTotalWithTax(invoice.getSubtotal(), invoice.getTaxAmount()));
        }
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToDto(savedInvoice);
    }

    @Override
    @Transactional
    @Auditable(entity = "Invoice", action = AuditLog.ActionType.UPDATE, description = "Update invoice")
    public InvoiceDto update(InvoiceDto invoiceDto) {
        Invoice existingInvoice = invoiceRepository.findById(invoiceDto.getId()).orElseThrow();
        
        existingInvoice.setIssueDate(invoiceDto.getIssueDate());
        existingInvoice.setDueDate(invoiceDto.getDueDate());
        existingInvoice.setSubtotal(invoiceDto.getSubtotal());
        existingInvoice.setTaxRate(invoiceDto.getTaxRate());
        existingInvoice.setTaxAmount(invoiceDto.getTaxAmount());
        existingInvoice.setTotalAmount(invoiceDto.getTotalAmount());
        existingInvoice.setStatus(invoiceDto.getStatus());
        existingInvoice.setPaymentTerms(invoiceDto.getPaymentTerms());
        existingInvoice.setNotes(invoiceDto.getNotes());
        existingInvoice.setReferenceNumber(invoiceDto.getReferenceNumber());

        Invoice savedInvoice = invoiceRepository.save(existingInvoice);
        return mapToDto(savedInvoice);
    }

    @Override
    public InvoiceDto getById(Long id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        return mapToDto(invoice);
    }

    @Override
    public List<InvoiceDto> getAll() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Auditable(entity = "Invoice", action = AuditLog.ActionType.DELETE, description = "Delete invoice")
    public void delete(Long id) {
        invoiceRepository.deleteById(id);
    }

    @Override
    public List<InvoiceDto> getByCustomerId(Long customerId) {
        List<Invoice> invoices = invoiceRepository.findByCustomerIdOrderByCreatedDateDesc(customerId);
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto> getByRentalId(Long rentalId) {
        List<Invoice> invoices = invoiceRepository.findByRentalIdOrderByCreatedDateDesc(rentalId);
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto> getPendingInvoices() {
        List<Invoice> invoices = invoiceRepository.findByStatus(Invoice.InvoiceStatus.PENDING);
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto> getOverdueInvoices() {
        List<Invoice> invoices = invoiceRepository.findOverdueInvoices(
                Invoice.InvoiceStatus.SENT, LocalDateTime.now());
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(entity = "Invoice", action = AuditLog.ActionType.UPDATE, description = "Mark invoice as sent")
    public InvoiceDto markAsSent(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();
        invoice.setStatus(Invoice.InvoiceStatus.SENT);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Send invoice email
        try {
            emailService.sendInvoiceNotification(invoice.getCustomer(), invoice);
        } catch (Exception e) {
            System.err.println("Error sending invoice email: " + e.getMessage());
        }
        
        return mapToDto(savedInvoice);
    }

    @Override
    @Transactional
    @Auditable(entity = "Invoice", action = AuditLog.ActionType.UPDATE, description = "Mark invoice as paid")
    public InvoiceDto markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToDto(savedInvoice);
    }

    @Override
    @Transactional
    public InvoiceDto markAsCancelled(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();
        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToDto(savedInvoice);
    }

    @Override
    @Transactional
    public InvoiceDto generateInvoiceForRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        
        // Check if invoice already exists for this rental
        List<Invoice> existingInvoices = invoiceRepository.findByRentalIdOrderByCreatedDateDesc(rentalId);
        if (!existingInvoices.isEmpty()) {
            throw new IllegalArgumentException("Invoice already exists for this rental");
        }
        
        // Create new invoice
        InvoiceDto invoiceDto = InvoiceDto.builder()
                .rentalId(rentalId)
                .customerId(rental.getCustomer().getId())
                .issueDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30)) // 30 days payment terms
                .subtotal(rental.getTotalAmount() != null ? rental.getTotalAmount() : BigDecimal.ZERO)
                .taxRate(BigDecimal.valueOf(0.18)) // 18% VAT
                .paymentTerms("30 gün")
                .build();
        
        return create(invoiceDto);
    }

    @Override
    public BigDecimal getTotalInvoicedAmount(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = invoiceRepository.getTotalInvoicedAmountBetween(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalPaidAmount(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = invoiceRepository.getTotalPaidAmountBetween(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalOverdueAmount() {
        BigDecimal total = invoiceRepository.getTotalOverdueAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<InvoiceDto> getInvoicesDueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        List<Invoice> invoices = invoiceRepository.findInvoicesDueBetween(startDate, endDate);
        return invoices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(
                Invoice.InvoiceStatus.SENT, LocalDateTime.now());
        
        for (Invoice invoice : overdueInvoices) {
            invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
            
            // Send overdue notification
            try {
                emailService.sendOverdueInvoiceNotification(invoice.getCustomer(), invoice);
            } catch (Exception e) {
                System.err.println("Error sending overdue invoice notification: " + e.getMessage());
            }
        }
    }

    @Override
    public BigDecimal calculateTaxAmount(BigDecimal subtotal, BigDecimal taxRate) {
        if (subtotal == null || taxRate == null) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalWithTax(BigDecimal subtotal, BigDecimal taxAmount) {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        if (taxAmount == null) {
            taxAmount = BigDecimal.ZERO;
        }
        return subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateInvoiceNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + timestamp.substring(timestamp.length() - 8) + "-" + uuid;
    }

    private InvoiceDto mapToDto(Invoice invoice) {
        InvoiceDto dto = modelMapper.map(invoice, InvoiceDto.class);
        
        // Set relationship IDs
        if (invoice.getCustomer() != null) {
            dto.setCustomerId(invoice.getCustomer().getId());
            dto.setCustomerName(invoice.getCustomer().getFirstName() + " " + invoice.getCustomer().getLastName());
            dto.setCustomerEmail(invoice.getCustomer().getEmail());
            // Construct address from available fields
            String address = String.format("%s, %d %s", 
                invoice.getCustomer().getStreet(), 
                invoice.getCustomer().getZipcode(), 
                invoice.getCustomer().getCity());
            dto.setCustomerAddress(address);
        }
        
        if (invoice.getRental() != null) {
            dto.setRentalId(invoice.getRental().getId());
            dto.setRentalStartDate(invoice.getRental().getStart().atStartOfDay());
            dto.setRentalEndDate(invoice.getRental().getEnd().atStartOfDay());
            dto.setRentalDays(ChronoUnit.DAYS.between(invoice.getRental().getStart(), invoice.getRental().getEnd()) + 1);
            
            if (invoice.getRental().getCar() != null) {
                dto.setCarPlate(invoice.getRental().getCar().getPlate());
                if (invoice.getRental().getCar().getModel() != null) {
                    dto.setCarModelName(invoice.getRental().getCar().getModel().getName());
                    if (invoice.getRental().getCar().getModel().getBrand() != null) {
                        dto.setCarBrandName(invoice.getRental().getCar().getModel().getBrand().getName());
                    }
                }
            }
        }
        
        // Set display name
        dto.setStatusDisplayName(invoice.getStatus().getDisplayName());
        
        // Calculate due date info
        if (invoice.getDueDate() != null) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), invoice.getDueDate());
            dto.setDaysUntilDue(daysUntilDue);
            dto.setDaysOverdue(Math.max(0, -daysUntilDue));
            dto.setIsOverdue(daysUntilDue < 0 && invoice.getStatus().equals(Invoice.InvoiceStatus.SENT));
        }
        
        return dto;
    }

    private Invoice mapToEntity(InvoiceDto dto) {
        Invoice invoice = modelMapper.map(dto, Invoice.class);
        
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow();
            invoice.setCustomer(customer);
        }
        
        if (dto.getRentalId() != null) {
            Rental rental = rentalRepository.findById(dto.getRentalId()).orElseThrow();
            invoice.setRental(rental);
        }
        
        return invoice;
    }

    @Override
    public InvoiceSummaryDto getSummary() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        BigDecimal totalAmount = allInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long count = allInvoices.size();
        
        BigDecimal pendingAmount = allInvoices.stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.PENDING)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal paidAmount = allInvoices.stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.PAID)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal overdueAmount = allInvoices.stream()
                .filter(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.OVERDUE)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, InvoiceSummaryDto.InvoiceStatusSummary> byStatus = new HashMap<>();
        
        for (Invoice.InvoiceStatus status : Invoice.InvoiceStatus.values()) {
            List<Invoice> statusInvoices = allInvoices.stream()
                    .filter(invoice -> invoice.getStatus() == status)
                    .collect(Collectors.toList());
            
            BigDecimal statusAmount = statusInvoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            byStatus.put(status.name(), InvoiceSummaryDto.InvoiceStatusSummary.builder()
                    .amount(statusAmount)
                    .count((long) statusInvoices.size())
                    .build());
        }
        
        return InvoiceSummaryDto.builder()
                .totalAmount(totalAmount)
                .count(count)
                .pendingAmount(pendingAmount)
                .paidAmount(paidAmount)
                .overdueAmount(overdueAmount)
                .byStatus(byStatus)
                .build();
    }
}
