package com.arturjarosz.fixmybudget.domain;

import com.arturjarosz.fixmybudget.application.Bank;
import com.arturjarosz.fixmybudget.domain.category.CategoryResolver;
import com.arturjarosz.fixmybudget.domain.mapper.RowToTransactionMapper;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import com.arturjarosz.fixmybudget.dto.CategoryDto;
import com.arturjarosz.fixmybudget.dto.SummaryDto;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CsvReaderService {
    private final CsvValidator csvValidator;
    private final AccountStatementFileProperties accountStatementFileProperties;
    private final RowToTransactionMapper rowToTransactionMapper;
    private final CategoryResolver categoryResolver;

    public AnalyzedStatementDto readCsv(MultipartFile file, Bank bank) {
        List<BankTransaction> bankTransactions = new ArrayList<>();
        var bankProperties = accountStatementFileProperties.banks()
                .get(bank);
        var parser = new CSVParserBuilder().withSeparator(bankProperties.delimiter()
                        .getDelimiter())
                .build();

        try (Reader reader = new InputStreamReader(file.getInputStream()); CSVReader csvReader = new CSVReaderBuilder(
                reader).withSkipLines(bankProperties.skipLines())
                .withCSVParser(parser)
                .build()) {
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IllegalArgumentException("CSV headers are empty");
            }
            csvValidator.checkFileHeaders(file, bank);
            String[] dataRow = null;
            while ((dataRow = csvReader.readNext()) != null) {
                var entity = rowToTransactionMapper.map(dataRow, bank);
                if (entity != null) {
                    bankTransactions.add(entity);
                }
            }
            log.info("Read {} rows from csv file", bankTransactions.size());
            categoryResolver.enrichWithCategories(bankTransactions, bank);
            log.info("Read {} rows with categories:", bankTransactions.stream()
                    .filter(bankTransaction -> bankTransaction.getCategory() != null)
                    .toList()
                    .size());


        } catch (IOException e) {
            throw new IllegalArgumentException("File is not csv or cannot be read");
        } catch (CsvException e) {
            throw new IllegalArgumentException("CSV file is not valid");
        }
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
