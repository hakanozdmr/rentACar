package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.TaxCalculation;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;
import hakan.rentacar.entities.dtos.TaxCalculationDto;
import hakan.rentacar.repostories.TaxCalculationRepository;
import hakan.rentacar.service.TaxCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TaxCalculationServiceImpl implements TaxCalculationService {

    @Autowired
    private TaxCalculationRepository taxCalculationRepository;

    @Override
    @Transactional
    public TaxCalculation calculateVAT(BigDecimal amount, BigDecimal customRate) {
        BigDecimal rate = customRate != null ? customRate : TaxCalculation.TaxType.VAT.getDefaultRate();
        BigDecimal taxAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        TaxCalculation calculation = TaxCalculation.builder()
                .taxType(TaxCalculation.TaxType.VAT)
                .taxableAmount(amount)
                .taxRate(rate)
                .taxAmount(taxAmount)
                .calculationDate(LocalDateTime.now())
                .taxPeriod(getCurrentTaxPeriod())
                .calculationDetails(String.format("KDV hesaplaması - %s%% oranında, %s tutarı üzerinden", 
                        rate.multiply(BigDecimal.valueOf(100)), amount))
                .build();
        
        return taxCalculationRepository.save(calculation);
    }

    @Override
    @Transactional
    public TaxCalculation calculateWithholdingTax(BigDecimal amount) {
        BigDecimal rate = TaxCalculation.TaxType.WITHHOLDING_TAX.getDefaultRate();
        BigDecimal taxAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        TaxCalculation calculation = TaxCalculation.builder()
                .taxType(TaxCalculation.TaxType.WITHHOLDING_TAX)
                .taxableAmount(amount)
                .taxRate(rate)
                .taxAmount(taxAmount)
                .calculationDate(LocalDateTime.now())
                .taxPeriod(getCurrentTaxPeriod())
                .calculationDetails(String.format("Stopaj hesaplaması - %s%% oranında", 
                        rate.multiply(BigDecimal.valueOf(100))))
                .build();
        
        return taxCalculationRepository.save(calculation);
    }

    @Override
    @Transactional
    public TaxCalculation calculateCorporateTax(BigDecimal profitAmount) {
        BigDecimal rate = TaxCalculation.TaxType.CORPORATE_TAX.getDefaultRate();
        BigDecimal taxAmount = profitAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        TaxCalculation calculation = TaxCalculation.builder()
                .taxType(TaxCalculation.TaxType.CORPORATE_TAX)
                .taxableAmount(profitAmount)
                .taxRate(rate)
                .taxAmount(taxAmount)
                .calculationDate(LocalDateTime.now())
                .taxPeriod(getCurrentTaxPeriod())
                .calculationDetails(String.format("Kurumlar vergisi hesaplaması - %s kar üzerinden %s%% oranında", 
                        profitAmount, rate.multiply(BigDecimal.valueOf(100))))
                .build();
        
        return taxCalculationRepository.save(calculation);
    }

    @Override
    @Transactional
    public void calculateTaxForInvoice(Invoice invoice) {
        if (invoice.getTaxAmount() == null || invoice.getTaxRate() == null) {
            return; // Skip if invoice doesn't have tax information
        }
        
        TaxCalculation calculation = TaxCalculation.builder()
                .invoice(invoice)
                .taxType(TaxCalculation.TaxType.VAT)
                .taxableAmount(invoice.getSubtotal())
                .taxRate(invoice.getTaxRate())
                .taxAmount(invoice.getTaxAmount())
                .calculationDate(LocalDateTime.now())
                .taxPeriod(getCurrentTaxPeriod())
                .calculationDetails(String.format("Fatura KDV hesaplaması - Fatura No: %s", invoice.getInvoiceNumber()))
                .build();
        
        taxCalculationRepository.save(calculation);
    }

    @Override
    @Transactional
    public void calculateTaxForPayment(Payment payment) {
        // Calculate withholding tax for certain payment types
        if (payment.getMethod() == Payment.PaymentMethod.BANK_TRANSFER) {
            calculateWithholdingTax(payment.getAmount());
        }
        
        // Create VAT calculation record for the payment (reverse calculation)
        if (payment.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
            // For large payments, calculate and record VAT
            BigDecimal vatRate = TaxCalculation.TaxType.VAT.getDefaultRate();
            BigDecimal amountWithoutVAT = payment.getAmount().divide(BigDecimal.ONE.add(vatRate), 2, RoundingMode.HALF_UP);
            BigDecimal vatAmount = payment.getAmount().subtract(amountWithoutVAT);
            
            TaxCalculation calculation = TaxCalculation.builder()
                    .payment(payment)
                    .taxType(TaxCalculation.TaxType.VAT)
                    .taxableAmount(amountWithoutVAT)
                    .taxRate(vatRate)
                    .taxAmount(vatAmount)
                    .calculationDate(LocalDateTime.now())
                    .taxPeriod(getCurrentTaxPeriod())
                    .calculationDetails(String.format("Ödeme KDV hesaplaması - Ödeme ID: %d", payment.getId()))
                    .build();
            
            taxCalculationRepository.save(calculation);
        }
    }

    @Override
    public Map<String, Object> getTaxReportForPeriod(String taxPeriod) {
        List<TaxCalculation> taxes = taxCalculationRepository.findByTaxPeriod(taxPeriod);
        Map<String, Object> report = new HashMap<>();
        
        BigDecimal totalVAT = BigDecimal.ZERO;
        BigDecimal totalWithholdingTax = BigDecimal.ZERO;
        BigDecimal totalCorporateTax = BigDecimal.ZERO;
        
        for (TaxCalculation tax : taxes) {
            switch (tax.getTaxType()) {
                case VAT:
                    totalVAT = totalVAT.add(tax.getTaxAmount());
                    break;
                case WITHHOLDING_TAX:
                    totalWithholdingTax = totalWithholdingTax.add(tax.getTaxAmount());
                    break;
                case CORPORATE_TAX:
                    totalCorporateTax = totalCorporateTax.add(tax.getTaxAmount());
                    break;
            }
        }
        
        report.put("taxPeriod", taxPeriod);
        report.put("totalVAT", totalVAT);
        report.put("totalWithholdingTax", totalWithholdingTax);
        report.put("totalCorporateTax", totalCorporateTax);
        report.put("totalTaxes", totalVAT.add(totalWithholdingTax).add(totalCorporateTax));
        report.put("taxCount", taxes.size());
        
        return report;
    }

    @Override
    public Map<String, Object> getVATReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<TaxCalculation> vatCalculations = taxCalculationRepository
                .findByTaxTypeAndCalculationDateBetween(TaxCalculation.TaxType.VAT, startDate, endDate);
        
        Map<String, Object> vatReport = new HashMap<>();
        BigDecimal totalVATCollected = BigDecimal.ZERO;
        BigDecimal totalVATPaid = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        
        for (TaxCalculation calculation : vatCalculations) {
            totalTaxAmount = totalTaxAmount.add(calculation.getTaxAmount());
            if (calculation.getInvoice() != null) {
                totalVATCollected = totalVATCollected.add(calculation.getTaxAmount());
            } else if (calculation.getPayment() != null) {
                totalVATPaid = totalVATPaid.add(calculation.getTaxAmount());
            }
        }
        
        BigDecimal netVAT = totalVATCollected.subtract(totalVATPaid);
        
        // Get current VAT rate
        BigDecimal vatRate = TaxCalculation.TaxType.VAT.getDefaultRate();
        
        vatReport.put("startDate", startDate);
        vatReport.put("endDate", endDate);
        vatReport.put("totalVATCollected", totalVATCollected);
        vatReport.put("totalVATPaid", totalVATPaid);
        vatReport.put("netVAT", netVAT);
        vatReport.put("vatCalculations", vatCalculations.size());
        vatReport.put("totalTaxAmount", totalTaxAmount);
        vatReport.put("vatRate", vatRate.multiply(BigDecimal.valueOf(100))); // Convert to percentage
        vatReport.put("totalTaxLiability", totalTaxAmount);
        
        return vatReport;
    }

    @Override
    public List<TaxCalculation> getUnreportedTaxes() {
        return taxCalculationRepository.findByIsReportedFalse();
    }

    @Override
    @Transactional
    public void markTaxAsReported(Long taxCalculationId) {
        TaxCalculation calculation = taxCalculationRepository.findById(taxCalculationId).orElseThrow();
        calculation.setIsReported(true);
        calculation.setReportedAt(LocalDateTime.now());
        taxCalculationRepository.save(calculation);
    }

    @Override
    public List<TaxCalculation> getTaxesByTypeAndPeriod(TaxCalculation.TaxType taxType, String taxPeriod) {
        return taxCalculationRepository.findByTaxTypeAndTaxPeriod(taxType, taxPeriod);
    }

    @Override
    public BigDecimal getCurrentTaxRate(TaxCalculation.TaxType taxType) {
        return taxType.getDefaultRate();
    }

    @Override
    public Map<TaxCalculation.TaxType, BigDecimal> getAllTaxRates() {
        Map<TaxCalculation.TaxType, BigDecimal> rates = new HashMap<>();
        for (TaxCalculation.TaxType type : TaxCalculation.TaxType.values()) {
            rates.put(type, type.getDefaultRate());
        }
        return rates;
    }

    @Override
    public BigDecimal calculateTotalTaxAmount(BigDecimal baseAmount, List<TaxCalculation.TaxType> applicableTaxes) {
        BigDecimal totalTax = BigDecimal.ZERO;
        for (TaxCalculation.TaxType taxType : applicableTaxes) {
            BigDecimal taxAmount = baseAmount.multiply(taxType.getDefaultRate()).setScale(2, RoundingMode.HALF_UP);
            totalTax = totalTax.add(taxAmount);
        }
        return totalTax;
    }

    @Override
    public Map<String, Object> getTaxSummaryByPeriod(String taxPeriod) {
        List<TaxCalculation> taxes = taxCalculationRepository.findByTaxPeriod(taxPeriod);
        Map<String, Object> summary = new HashMap<>();
        
        Map<TaxCalculation.TaxType, BigDecimal> taxTotals = new HashMap<>();
        Map<TaxCalculation.TaxType, Integer> taxCounts = new HashMap<>();
        
        for (TaxCalculation.TaxType type : TaxCalculation.TaxType.values()) {
            taxTotals.put(type, BigDecimal.ZERO);
            taxCounts.put(type, 0);
        }
        
        for (TaxCalculation tax : taxes) {
            taxTotals.put(tax.getTaxType(), taxTotals.get(tax.getTaxType()).add(tax.getTaxAmount()));
            taxCounts.put(tax.getTaxType(), taxCounts.get(tax.getTaxType()) + 1);
        }
        
        summary.put("taxPeriod", taxPeriod);
        summary.put("taxTotals", taxTotals);
        summary.put("taxCounts", taxCounts);
        summary.put("totalCalculations", taxes.size());
        
        return summary;
    }

    private String getCurrentTaxPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
