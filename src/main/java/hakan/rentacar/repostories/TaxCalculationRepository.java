package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.TaxCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {
    
    List<TaxCalculation> findByTaxType(TaxCalculation.TaxType taxType);
    
    List<TaxCalculation> findByTaxPeriod(String taxPeriod);
    
    List<TaxCalculation> findByTaxTypeAndTaxPeriod(TaxCalculation.TaxType taxType, String taxPeriod);
    
    List<TaxCalculation> findByIsReportedFalse();
    
    List<TaxCalculation> findByCalculationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT tc FROM TaxCalculation tc WHERE tc.taxType = :taxType AND tc.calculationDate BETWEEN :startDate AND :endDate")
    List<TaxCalculation> findByTaxTypeAndCalculationDateBetween(@Param("taxType") TaxCalculation.TaxType taxType, 
                                                               @Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(tc.taxAmount) FROM TaxCalculation tc WHERE tc.taxType = :taxType AND tc.taxPeriod = :taxPeriod")
    BigDecimal getTotalTaxAmountByTypeAndPeriod(@Param("taxType") TaxCalculation.TaxType taxType, 
                                               @Param("taxPeriod") String taxPeriod);
    
    @Query("SELECT tc.taxType, SUM(tc.taxAmount), COUNT(tc) FROM TaxCalculation tc WHERE tc.taxPeriod = :taxPeriod GROUP BY tc.taxType")
    List<Object[]> getTaxSummaryByPeriod(@Param("taxPeriod") String taxPeriod);
    
    List<TaxCalculation> findByInvoiceId(Long invoiceId);
    
    List<TaxCalculation> findByPaymentId(Long paymentId);
}




