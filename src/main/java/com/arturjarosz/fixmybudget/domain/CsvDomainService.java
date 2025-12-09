package com.arturjarosz.fixmybudget.domain;

import com.arturjarosz.fixmybudget.application.Bank;
import com.arturjarosz.fixmybudget.domain.category.CategoryResolver;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.domain.repository.BankTransactionRepository;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import com.arturjarosz.fixmybudget.dto.CategoryDto;
import com.arturjarosz.fixmybudget.dto.SummaryDto;
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
public class CsvDomainService {

    private final CategoryResolver categoryResolver;
    private final BankTransactionRepository bankTransactionRepository;
    private final CsvReaderService csvReaderService;

    public AnalyzedStatementDto readCsv(MultipartFile file, Bank bank, String source) {
        var bankTransactions = csvReaderService.readCsv(file, bank, source);

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

        categoryResolver.enrichWithCategories(bankTransactions, bank);

        return buildResponse(bankTransactions);
    }

    private AnalyzedStatementDto buildResponse(List<BankTransaction> bankTransactions) {
        var response = new LinkedHashMap<String, CategoryDto>();
        var summary = new LinkedHashMap<String, SummaryDto>();
        var transactionsByCategory = bankTransactions.stream()
                .collect(Collectors.groupingBy(BankTransaction::getCategory));
        for (String category : transactionsByCategory.keySet()) {
            var sum = transactionsByCategory.get(category)
                    .stream()
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b.getAmount()), BigDecimal::add);
            response.putIfAbsent(category, new CategoryDto(sum, transactionsByCategory.get(category)));
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
}
