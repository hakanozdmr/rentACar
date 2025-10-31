package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {
    
    @Query("SELECT gl FROM GeneralLedger gl ORDER BY gl.transactionDate ASC, gl.id ASC")
    List<GeneralLedger> findAllOrderByTransactionDateAndId();
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.accountType = :accountType ORDER BY gl.transactionDate ASC")
    List<GeneralLedger> findByAccountTypeOrderByTransactionDate(@Param("accountType") GeneralLedger.AccountType accountType);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.transactionType = :transactionType ORDER BY gl.transactionDate DESC")
    List<GeneralLedger> findByTransactionTypeOrderByTransactionDateDesc(@Param("transactionType") GeneralLedger.TransactionType transactionType);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.transactionDate BETWEEN :startDate AND :endDate ORDER BY gl.transactionDate ASC")
    List<GeneralLedger> findByTransactionDateBetweenOrderByTransactionDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.referenceType = :referenceType AND gl.referenceId = :referenceId")
    List<GeneralLedger> findByReferenceTypeAndReferenceId(@Param("referenceType") String referenceType, @Param("referenceId") Long referenceId);
    
    // Balance calculations
    @Query("SELECT COALESCE(SUM(gl.debitAmount), 0) FROM GeneralLedger gl WHERE gl.accountType = :accountType")
    BigDecimal getTotalDebitsByAccountType(@Param("accountType") GeneralLedger.AccountType accountType);
    
    @Query("SELECT COALESCE(SUM(gl.creditAmount), 0) FROM GeneralLedger gl WHERE gl.accountType = :accountType")
    BigDecimal getTotalCreditsByAccountType(@Param("accountType") GeneralLedger.AccountType accountType);
    
    // Trial Balance
    @Query("SELECT gl.accountType, gl.accountCode, gl.accountName, " +
           "SUM(gl.debitAmount), SUM(gl.creditAmount) " +
           "FROM GeneralLedger gl " +
           "GROUP BY gl.accountType, gl.accountCode, gl.accountName " +
           "ORDER BY gl.accountType, gl.accountCode")
    List<Object[]> getTrialBalance();
    
    // Income Statement (P&L)
    @Query("SELECT gl.transactionType, SUM(gl.creditAmount), SUM(gl.debitAmount) " +
           "FROM GeneralLedger gl " +
           "WHERE gl.transactionDate BETWEEN :startDate AND :endDate " +
           "AND gl.transactionType IN ('REVENUE', 'EXPENSE') " +
           "GROUP BY gl.transactionType")
    List<Object[]> getIncomeStatementData(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Balance Sheet data
    @Query("SELECT gl.accountType, SUM(gl.debitAmount), SUM(gl.creditAmount) " +
           "FROM GeneralLedger gl " +
           "WHERE gl.transactionDate <= :date " +
           "GROUP BY gl.accountType")
    List<Object[]> getBalanceSheetData(@Param("date") LocalDateTime date);
    
    // Cash flow summary
    @Query("SELECT SUM(gl.debitAmount), SUM(gl.creditAmount) " +
           "FROM GeneralLedger gl " +
           "WHERE gl.accountType IN ('CASH_ASSET', 'BANK_ASSET') " +
           "AND gl.transactionDate BETWEEN :startDate AND :endDate")
    Object[] getCashFlowSummary(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Unreconciled transactions
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.reconciled = false ORDER BY gl.transactionDate ASC")
    List<GeneralLedger> findUnreconciledTransactions();
}


