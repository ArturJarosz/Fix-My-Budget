package com.arturjarosz.fixmybudget.transaction;

import com.arturjarosz.fixmybudget.category.CategoryResolver;
import com.arturjarosz.fixmybudget.csv.CsvReaderService;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.dto.CategorySummaryDto;
import com.arturjarosz.fixmybudget.dto.OverrideCategoryDto;
import com.arturjarosz.fixmybudget.dto.SummaryDto;
import com.arturjarosz.fixmybudget.transaction.dto.CategorySummary;
import com.arturjarosz.fixmybudget.transaction.dto.TransactionsSummary;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import com.arturjarosz.fixmybudget.transaction.repository.BankTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BankTransactionDomainService {

    private final CategoryResolver categoryResolver;
    private final BankTransactionRepository bankTransactionRepository;
    private final CsvReaderService csvReaderService;

    public AnalyzedStatementDto processCsv(MultipartFile file, Bank bank, String source) {
        log.info("Processing CSV file for bank {} from source {}.", bank, source);
        var bankTransactions = csvReaderService.readCsv(file, bank, source);
        bankTransactions.forEach(bankTransaction -> bankTransaction.setBank(bank));

        categoryResolver.enrichWithCategories(bankTransactions, bank);

        var allStoredBankTransactions = bankTransactionRepository.findAll();
        var missingTransactions = bankTransactions.stream()
                .filter(bankTransaction -> !allStoredBankTransactions.contains(bankTransaction))
                .toList();

        log.info("Saving {} missing transactions", missingTransactions.size());

        bankTransactionRepository.saveAll(missingTransactions);

        return buildResponse(bankTransactions);
    }

    public AnalyzedStatementDto calculateCategories(Bank bank) {
        var bankTransactions = bankTransactionRepository.findAll();
        log.info("Calculating [{}] categories for bank [{}] transactions", bank.name(), bankTransactions.size());

        bankTransactions = bankTransactions.stream()
                .filter(bankTransaction -> bankTransaction.getBank()
                        .equals(bank))
                .filter(bankTransaction -> bankTransaction.getCategoryOverridden() == null || !bankTransaction.getCategoryOverridden())
                .toList();
        categoryResolver.enrichWithCategories(bankTransactions, bank);
        bankTransactionRepository.saveAll(bankTransactions);

        return buildResponse(bankTransactions);
    }

    private AnalyzedStatementDto buildResponse(List<BankTransaction> bankTransactions) {
        var response = new LinkedHashMap<String, CategorySummaryDto>();
        var summary = new LinkedHashMap<String, SummaryDto>();
        var transactionsByCategory = bankTransactions.stream()
                .collect(Collectors.groupingBy(BankTransaction::getCategory));
        for (String category : transactionsByCategory.keySet()) {
            var sum = transactionsByCategory.get(category)
                    .stream()
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b.getAmount()), BigDecimal::add);
            response.putIfAbsent(category, new CategorySummaryDto(sum, transactionsByCategory.get(category)));
            summary.putIfAbsent(category, new SummaryDto(transactionsByCategory.get(category)
                    .size(), sum));
        }
        var sortedSummary = summary.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // natural order of keys
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));

        return AnalyzedStatementDto.builder()
                .summary(sortedSummary)
                .transactionsByCategory(response)
                .build();
    }

    public BankTransaction overrideCategory(Long bankTransactionId, OverrideCategoryDto overrideCategoryDto) {
        log.info(" Overriding category for bank transaction {}.", bankTransactionId);
        var maybeBankTransaction = bankTransactionRepository.findById(bankTransactionId);
        if (maybeBankTransaction.isEmpty()) {
            throw new EntityNotFoundException("Bank transaction with id " + bankTransactionId + " not found");
        }

        var bankTransaction = maybeBankTransaction.get();
        bankTransaction.setCategory(overrideCategoryDto.getCategory());
        bankTransaction.setCategoryOverridden(true);

        return bankTransaction;
    }

    public TransactionsSummary getTransactionsSummary() {
        log.info("Getting transactions summary.");
        var allTransactions = this.bankTransactionRepository.findAll();
        var transactionsByBankAndCategory = allTransactions.stream()
                .collect(Collectors.groupingBy(BankTransaction::getBank,
                        Collectors.groupingBy(BankTransaction::getCategory)));

        Map<Bank, Map<TransactionType, List<CategorySummary>>> summaryByBank = new HashMap<>();


        transactionsByBankAndCategory.entrySet()
                .forEach(entry -> {
            var bank = entry.getKey();
            var transactionsByCategory = entry.getValue();
            var summaryByTransactionType = getSummaryByTransactionType(transactionsByCategory);
            summaryByBank.put(bank, summaryByTransactionType);
        });

        return TransactionsSummary.builder()
                .categorySummaryByTypeByBank(summaryByBank)
                .allBanksCategorySummaries(getAllBanksSummary(summaryByBank))
                .build();
    }


    private Map<TransactionType, List<CategorySummary>> getSummaryByTransactionType(Map<String, List<BankTransaction>> transactionsByCategory) {
        var summaryByTransactionType = new HashMap<TransactionType, List<CategorySummary>>();
        transactionsByCategory.forEach((categoryName, transactions) -> {
            var categorySummary = getCategorySummary(categoryName, transactions);
            var transactionType = TransactionType.EXPENSE;
            if (!transactions.isEmpty()) {
                transactionType = transactions.getFirst()
                        .getTransactionType();
            }
            summaryByTransactionType.computeIfAbsent(transactionType, k -> new ArrayList<>()).add(categorySummary);
        });
        return summaryByTransactionType;
    }

    private Map<TransactionType, List<CategorySummary>> getAllBanksSummary(Map<Bank, Map<TransactionType, List<CategorySummary>>> categorySummaryByTypeByBank) {
        var summaryByTransactionType = new HashMap<TransactionType, Set<CategorySummary>>();
        summaryByTransactionType.put(TransactionType.INCOME, new HashSet<>());
        summaryByTransactionType.put(TransactionType.EXPENSE, new HashSet<>());
        categorySummaryByTypeByBank.forEach((bank, transactionTypeSummary) -> {
            transactionTypeSummary.forEach((transactionType, categorySummaries) -> {
                categorySummaries.forEach(summary -> {
                    if (summaryByTransactionType.get(TransactionType.INCOME).contains(summary)) {
                        var toMerge = summaryByTransactionType.get(TransactionType.INCOME).stream()
                                .filter(s -> s.equals(summary))
                                .findFirst()
                                .orElse(null);
                        summaryByTransactionType.get(TransactionType.INCOME).remove(toMerge);
                        summaryByTransactionType.get(TransactionType.INCOME).add(mergeSummaries(toMerge, summary));
                    } else if (summaryByTransactionType.get(TransactionType.EXPENSE).contains(summary)) {
                        var toMerge = summaryByTransactionType.get(TransactionType.EXPENSE).stream()
                                .filter(s -> s.equals(summary))
                                .findFirst()
                                .orElse(null);
                        summaryByTransactionType.get(TransactionType.EXPENSE).remove(toMerge);
                        summaryByTransactionType.get(TransactionType.EXPENSE).add(mergeSummaries(toMerge, summary));
                    } else {
                        summaryByTransactionType.get(summary.type()).add(summary);
                    }
                });
            });
        });
        var result = new HashMap<TransactionType, List<CategorySummary>>();
        result.put(TransactionType.INCOME, new ArrayList<>(summaryByTransactionType.get(TransactionType.INCOME)));
        result.put(TransactionType.EXPENSE, new ArrayList<>(summaryByTransactionType.get(TransactionType.EXPENSE)));
        return result;
    }

    private CategorySummary mergeSummaries(CategorySummary main, CategorySummary toMerge) {
        if (main == null) return toMerge;
        if (toMerge == null) return main;
        return CategorySummary.builder()
                .name(main.name())
                .count(main.count() + toMerge.count())
                .totalAmount(main.totalAmount().add(toMerge.totalAmount()))
                .type(main.type())
                .build();
    }


    private CategorySummary getCategorySummary(String categoryName, List<BankTransaction> transactions) {
        var summary = new Summary();
        transactions.forEach(transaction -> summary.increase(transaction.getAmount()));
        var transactionType = TransactionType.EXPENSE;
        if (!transactions.isEmpty()) {
            transactionType = transactions.getFirst()
                    .getTransactionType();
        }

        return new CategorySummary(categoryName, summary.count, summary.totalValue, transactionType);
    }

    @Getter
    private class Summary {
        private int count = 0;
        private BigDecimal totalValue = BigDecimal.ZERO;

        public void increase(BigDecimal value) {
            count++;
            totalValue = totalValue.add(value);
        }
    }
}
