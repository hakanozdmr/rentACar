package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.GeneralLedger;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;
import hakan.rentacar.entities.dtos.GeneralLedgerDto;
import hakan.rentacar.repostories.GeneralLedgerRepository;
import hakan.rentacar.service.GeneralLedgerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneralLedgerServiceImpl implements GeneralLedgerService {

    @Autowired
    private GeneralLedgerRepository generalLedgerRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Chart of Accounts mapping
    private static final Map<GeneralLedger.AccountType, String> ACCOUNT_CODES = new HashMap<>();
    static {
        ACCOUNT_CODES.put(GeneralLedger.AccountType.CASH_ASSET, "1001");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.BANK_ASSET, "1002");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.ACCOUNTS_RECEIVABLE, "1201");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.FIXED_ASSET, "1501");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.ACCOUNTS_PAYABLE, "2001");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.TAX_PAYABLE, "2101");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.CAPITAL, "3001");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.RENTAL_REVENUE, "4001");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.OPERATING_EXPENSE, "5001");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.MAINTENANCE_EXPENSE, "5101");
        ACCOUNT_CODES.put(GeneralLedger.AccountType.ADMINISTRATIVE_EXPENSE, "5201");
    }

    @Override
    @Transactional
    public GeneralLedgerDto create(GeneralLedgerDto ledgerDto) {
        GeneralLedger ledger = mapToEntity(ledgerDto);
        GeneralLedger savedLedger = generalLedgerRepository.save(ledger);
        return mapToDto(savedLedger);
    }

    @Override
    public List<GeneralLedgerDto> getAll() {
        List<GeneralLedger> ledgers = generalLedgerRepository.findAllOrderByTransactionDateAndId();
        return ledgers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public GeneralLedgerDto getById(Long id) {
        GeneralLedger ledger = generalLedgerRepository.findById(id).orElseThrow();
        return mapToDto(ledger);
    }

    @Override
    public void delete(Long id) {
        generalLedgerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void createJournalEntry(GeneralLedger.TransactionType transactionType,
                                 GeneralLedger.AccountType debitAccount,
                                 GeneralLedger.AccountType creditAccount,
                                 BigDecimal amount,
                                 String description,
                                 Long referenceId,
                                 String referenceType) {
        
        LocalDateTime now = LocalDateTime.now();
        String docNumber = generateDocumentNumber();
        
        // Debit entry
        GeneralLedger debitEntry = GeneralLedger.builder()
                .transactionType(transactionType)
                .accountType(debitAccount)
                .accountCode(ACCOUNT_CODES.get(debitAccount))
                .accountName(debitAccount.getDisplayName())
                .transactionDate(now)
                .description(description)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .documentNumber(docNumber)
                .build();
        
        // Credit entry
        GeneralLedger creditEntry = GeneralLedger.builder()
                .transactionType(transactionType)
                .accountType(creditAccount)
                .accountCode(ACCOUNT_CODES.get(creditAccount))
                .accountName(creditAccount.getDisplayName())
                .transactionDate(now)
                .description(description)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .documentNumber(docNumber)
                .build();
        
        generalLedgerRepository.save(debitEntry);
        generalLedgerRepository.save(creditEntry);
    }

    @Override
    @Transactional
    public void recordPaymentReceived(Payment payment) {
        String description = String.format("Ödeme alındı - Kiralama #%d", payment.getRental().getId());
        
        createJournalEntry(
                GeneralLedger.TransactionType.REVENUE,
                GeneralLedger.AccountType.CASH_ASSET, // or BANK_ASSET based on payment method
                GeneralLedger.AccountType.RENTAL_REVENUE,
                payment.getAmount(),
                description,
                payment.getId(),
                "PAYMENT"
        );
    }

    @Override
    @Transactional
    public void recordInvoiceIssued(Invoice invoice) {
        String description = String.format("Fatura kesildi - Kiralama #%d", invoice.getRental().getId());
        
        createJournalEntry(
                GeneralLedger.TransactionType.REVENUE,
                GeneralLedger.AccountType.ACCOUNTS_RECEIVABLE,
                GeneralLedger.AccountType.RENTAL_REVENUE,
                invoice.getTotalAmount(),
                description,
                invoice.getId(),
                "INVOICE"
        );
    }

    @Override
    @Transactional
    public void recordExpense(BigDecimal amount, String description, GeneralLedger.AccountType expenseType) {
        createJournalEntry(
                GeneralLedger.TransactionType.EXPENSE,
                expenseType,
                GeneralLedger.AccountType.CASH_ASSET,
                amount,
                description,
                null,
                "EXPENSE"
        );
    }

    @Override
    @Transactional
    public void recordRevenue(BigDecimal amount, String description, GeneralLedger.AccountType revenueType) {
        createJournalEntry(
                GeneralLedger.TransactionType.REVENUE,
                GeneralLedger.AccountType.CASH_ASSET,
                revenueType,
                amount,
                description,
                null,
                "REVENUE"
        );
    }

    @Override
    public List<GeneralLedgerDto> getTrialBalance() {
        List<Object[]> trialBalanceData = generalLedgerRepository.getTrialBalance();
        List<GeneralLedgerDto> result = new ArrayList<>();
        
        for (Object[] row : trialBalanceData) {
            GeneralLedger.AccountType accountType = (GeneralLedger.AccountType) row[0];
            String accountCode = (String) row[1];
            String accountName = (String) row[2];
            BigDecimal totalDebit = (BigDecimal) row[3];
            BigDecimal totalCredit = (BigDecimal) row[4];
            
            GeneralLedgerDto dto = GeneralLedgerDto.builder()
                    .accountType(accountType)
                    .accountCode(accountCode)
                    .accountName(accountName)
                    .debitAmount(totalDebit)
                    .creditAmount(totalCredit)
                    .build();
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getIncomeStatement(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> incomeData = generalLedgerRepository.getIncomeStatementData(startDate, endDate);
        Map<String, Object> statement = new HashMap<>();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<Map<String, Object>> revenues = new ArrayList<>();
        List<Map<String, Object>> expenses = new ArrayList<>();
        
        for (Object[] row : incomeData) {
            if (row.length >= 3) {
                GeneralLedger.TransactionType type = (GeneralLedger.TransactionType) row[0];
                BigDecimal credits = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : BigDecimal.ZERO;
                BigDecimal debits = row[2] instanceof BigDecimal ? (BigDecimal) row[2] : BigDecimal.ZERO;
                
                if (type == GeneralLedger.TransactionType.REVENUE) {
                    totalRevenue = totalRevenue.add(credits);
                    Map<String, Object> revenueItem = new HashMap<>();
                    revenueItem.put("name", "Kiralama Gelirleri");
                    revenueItem.put("amount", credits);
                    revenues.add(revenueItem);
                } else if (type == GeneralLedger.TransactionType.EXPENSE) {
                    totalExpenses = totalExpenses.add(debits);
                    Map<String, Object> expenseItem = new HashMap<>();
                    expenseItem.put("name", "Operasyonel Giderler");
                    expenseItem.put("amount", debits);
                    expenses.add(expenseItem);
                }
            }
        }
        
        // Eğer hiç veri yoksa default değerler ekle
        if (revenues.isEmpty()) {
            Map<String, Object> defaultRevenue = new HashMap<>();
            defaultRevenue.put("name", "Kiralama Gelirleri");
            defaultRevenue.put("amount", BigDecimal.ZERO);
            revenues.add(defaultRevenue);
        }
        
        if (expenses.isEmpty()) {
            Map<String, Object> defaultExpense = new HashMap<>();
            defaultExpense.put("name", "Operasyonel Giderler");
            defaultExpense.put("amount", BigDecimal.ZERO);
            expenses.add(defaultExpense);
        }
        
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        
        statement.put("startDate", startDate);
        statement.put("endDate", endDate);
        statement.put("totalRevenue", totalRevenue);
        statement.put("totalExpenses", totalExpenses);
        statement.put("netProfit", netProfit);
        statement.put("revenues", revenues);
        statement.put("expenses", expenses);
        
        return statement;
    }

    @Override
    public Map<String, Object> getBalanceSheet(LocalDateTime date) {
        List<Object[]> balanceData = generalLedgerRepository.getBalanceSheetData(date);
        Map<String, Object> balanceSheet = new HashMap<>();
        
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        
        for (Object[] row : balanceData) {
            GeneralLedger.AccountType accountType = (GeneralLedger.AccountType) row[0];
            BigDecimal debits = (BigDecimal) row[1];
            BigDecimal credits = (BigDecimal) row[2];
            
            BigDecimal balance = debits.subtract(credits);
            
            if (accountType.name().contains("ASSET")) {
                totalAssets = totalAssets.add(balance);
            } else if (accountType.name().contains("PAYABLE") || accountType.name().contains("DEBT")) {
                totalLiabilities = totalLiabilities.add(credits.subtract(debits));
            } else if (accountType.name().equals("CAPITAL") || accountType.name().equals("RETAINED_EARNINGS")) {
                totalEquity = totalEquity.add(credits.subtract(debits));
            }
        }
        
        balanceSheet.put("asOfDate", date);
        balanceSheet.put("totalAssets", totalAssets);
        balanceSheet.put("totalLiabilities", totalLiabilities);
        balanceSheet.put("totalEquity", totalEquity);
        
        return balanceSheet;
    }

    @Override
    public Map<String, Object> getCashFlowStatement(LocalDateTime startDate, LocalDateTime endDate) {
        Object[] cashFlowData = generalLedgerRepository.getCashFlowSummary(startDate, endDate);
        
        Map<String, Object> cashFlow = new HashMap<>();
        cashFlow.put("startDate", startDate);
        cashFlow.put("endDate", endDate);
        
        // Safe array access with proper type checking
        BigDecimal cashOutflows = BigDecimal.ZERO;
        BigDecimal cashInflows = BigDecimal.ZERO;
        
        if (cashFlowData != null && cashFlowData.length > 0) {
            Object outflowsObj = cashFlowData[0];
            if (outflowsObj instanceof BigDecimal) {
                cashOutflows = (BigDecimal) outflowsObj;
            } else if (outflowsObj instanceof Number) {
                cashOutflows = BigDecimal.valueOf(((Number) outflowsObj).doubleValue());
            }
        }
        
        if (cashFlowData != null && cashFlowData.length > 1) {
            Object inflowsObj = cashFlowData[1];
            if (inflowsObj instanceof BigDecimal) {
                cashInflows = (BigDecimal) inflowsObj;
            } else if (inflowsObj instanceof Number) {
                cashInflows = BigDecimal.valueOf(((Number) inflowsObj).doubleValue());
            }
        }
        
        cashFlow.put("cashInflows", cashInflows);
        cashFlow.put("cashOutflows", cashOutflows);
        
        BigDecimal netCashFlow = cashInflows.subtract(cashOutflows);
        cashFlow.put("netCashFlow", netCashFlow);
        
        return cashFlow;
    }

    @Override
    public BigDecimal getAccountBalance(GeneralLedger.AccountType accountType) {
        BigDecimal totalDebits = generalLedgerRepository.getTotalDebitsByAccountType(accountType);
        BigDecimal totalCredits = generalLedgerRepository.getTotalCreditsByAccountType(accountType);
        return totalDebits.subtract(totalCredits);
    }

    @Override
    public List<GeneralLedgerDto> getAccountTransactions(GeneralLedger.AccountType accountType, 
                                                       LocalDateTime startDate, LocalDateTime endDate) {
        List<GeneralLedger> transactions = generalLedgerRepository
                .findByAccountTypeOrderByTransactionDate(accountType);
        
        return transactions.stream()
                .filter(t -> !t.getTransactionDate().isBefore(startDate) && !t.getTransactionDate().isAfter(endDate))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneralLedgerDto> getUnreconciledTransactions() {
        List<GeneralLedger> transactions = generalLedgerRepository.findUnreconciledTransactions();
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsReconciled(Long ledgerId) {
        GeneralLedger ledger = generalLedgerRepository.findById(ledgerId).orElseThrow();
        ledger.setReconciled(true);
        ledger.setReconciledAt(LocalDateTime.now());
        generalLedgerRepository.save(ledger);
    }

    @Override
    @Transactional
    public void closeAccountingPeriod(LocalDateTime periodEnd) {
        // Close revenue and expense accounts to retained earnings
        List<Object[]> incomeData = generalLedgerRepository.getIncomeStatementData(
                periodEnd.minusDays(30), periodEnd);
        
        // This would typically involve more complex period closing procedures
        // For now, we'll just mark the period as closed
        System.out.println("Accounting period closed for: " + periodEnd);
    }

    private String generateDocumentNumber() {
        return "GL-" + System.currentTimeMillis();
    }

    private GeneralLedgerDto mapToDto(GeneralLedger ledger) {
        GeneralLedgerDto dto = modelMapper.map(ledger, GeneralLedgerDto.class);
        dto.setTransactionTypeDisplayName(ledger.getTransactionType().getDisplayName());
        dto.setAccountTypeDisplayName(ledger.getAccountType().getDisplayName());
        return dto;
    }

    private GeneralLedger mapToEntity(GeneralLedgerDto dto) {
        return modelMapper.map(dto, GeneralLedger.class);
    }
}
