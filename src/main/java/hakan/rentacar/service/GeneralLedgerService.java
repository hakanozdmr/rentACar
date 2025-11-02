package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.GeneralLedgerDto;
import hakan.rentacar.entities.concretes.GeneralLedger;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface GeneralLedgerService {
    
    // Basic CRUD operations
    GeneralLedgerDto create(GeneralLedgerDto ledgerDto);
    List<GeneralLedgerDto> getAll();
    GeneralLedgerDto getById(Long id);
    void delete(Long id);
    
    // Journal entry creation
    void createJournalEntry(GeneralLedger.TransactionType transactionType, 
                           GeneralLedger.AccountType debitAccount,
                           GeneralLedger.AccountType creditAccount,
                           BigDecimal amount, 
                           String description,
                           Long referenceId,
                           String referenceType);
    
    // Automatic journal entries for business transactions
    void recordPaymentReceived(Payment payment);
    void recordInvoiceIssued(Invoice invoice);
    void recordExpense(BigDecimal amount, String description, GeneralLedger.AccountType expenseType);
    void recordRevenue(BigDecimal amount, String description, GeneralLedger.AccountType revenueType);
    
    // Financial reporting
    List<GeneralLedgerDto> getTrialBalance();
    Map<String, Object> getIncomeStatement(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Object> getBalanceSheet(LocalDateTime date);
    Map<String, Object> getCashFlowStatement(LocalDateTime startDate, LocalDateTime endDate);
    
    // Account balances
    BigDecimal getAccountBalance(GeneralLedger.AccountType accountType);
    List<GeneralLedgerDto> getAccountTransactions(GeneralLedger.AccountType accountType, 
                                                 LocalDateTime startDate, LocalDateTime endDate);
    
    // Reconciliation
    List<GeneralLedgerDto> getUnreconciledTransactions();
    void markAsReconciled(Long ledgerId);
    
    // Period closing
    void closeAccountingPeriod(LocalDateTime periodEnd);
}




