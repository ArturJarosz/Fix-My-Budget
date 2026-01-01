package com.arturjarosz.fixmybudget.transaction;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.dto.OverrideCategoryDto;
import com.arturjarosz.fixmybudget.category.CategoryResolver;
import com.arturjarosz.fixmybudget.csv.CsvReaderService;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.transaction.repository.BankTransactionRepository;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import com.arturjarosz.fixmybudget.dto.CategorySummaryDto;
import com.arturjarosz.fixmybudget.dto.SummaryDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BankTransactionDomainService {

    private final CategoryResolver categoryResolver;
    private final BankTransactionRepository bankTransactionRepository;
    private final CsvReaderService csvReaderService;

    public AnalyzedStatementDto processCsv(MultipartFile file, Bank bank, String source) {
        log.info(" Processing CSV file for bank {} from source {}.", bank, source);
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
                .filter(bankTransaction -> bankTransaction.getBank().equals(bank))
                .filter(bankTransaction -> bankTransaction.getCategoryOverridden() == null || !bankTransaction.getCategoryOverridden())
                .toList();
        categoryResolver.enrichWithCategories(bankTransactions, bank);

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
}
