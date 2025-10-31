package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.TaxCalculation;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TaxCalculationService {
    
    // Tax calculation operations
    TaxCalculation calculateVAT(BigDecimal amount, BigDecimal customRate);
    TaxCalculation calculateWithholdingTax(BigDecimal amount);
    TaxCalculation calculateCorporateTax(BigDecimal profitAmount);
    
    // Automatic tax calculations for business transactions
    void calculateTaxForInvoice(Invoice invoice);
    void calculateTaxForPayment(Payment payment);
    
    // Tax reporting
    Map<String, Object> getTaxReportForPeriod(String taxPeriod);
    Map<String, Object> getVATReport(LocalDateTime startDate, LocalDateTime endDate);
    List<TaxCalculation> getUnreportedTaxes();
    
    // Tax compliance
    void markTaxAsReported(Long taxCalculationId);
    List<TaxCalculation> getTaxesByTypeAndPeriod(TaxCalculation.TaxType taxType, String taxPeriod);
    
    // Tax rates and regulations
    BigDecimal getCurrentTaxRate(TaxCalculation.TaxType taxType);
    Map<TaxCalculation.TaxType, BigDecimal> getAllTaxRates();
    
    // Special calculations
    BigDecimal calculateTotalTaxAmount(BigDecimal baseAmount, List<TaxCalculation.TaxType> applicableTaxes);
    Map<String, Object> getTaxSummaryByPeriod(String taxPeriod);
}


