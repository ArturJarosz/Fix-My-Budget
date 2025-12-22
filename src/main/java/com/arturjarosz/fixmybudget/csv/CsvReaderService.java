package com.arturjarosz.fixmybudget.csv;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.transaction.mapper.RowToTransactionMapper;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.csv.validator.CsvValidator;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CsvReaderService {
    private final CsvValidator csvValidator;
    private final AccountStatementFileProperties accountStatementFileProperties;
    private final RowToTransactionMapper rowToTransactionMapper;

    public List<BankTransaction> readCsv(MultipartFile file, Bank bank, String source) {
        List<BankTransaction> bankTransactions = new ArrayList<>();
        var bankProperties = accountStatementFileProperties.banks()
                .get(bank);
        var parser = new CSVParserBuilder().withSeparator(bankProperties.delimiter()
                        .getCharacter())
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
                    entity.setSource(source);
                    bankTransactions.add(entity);
                    entity.setTransactionHash(entity.generateDbHashCode());
                }
            }
            log.info("Read {} rows from csv file", bankTransactions.size());
            return bankTransactions;


        } catch (IOException e) {
            throw new IllegalArgumentException("File is not csv or cannot be read");
        } catch (CsvException e) {
            throw new IllegalArgumentException("CSV file is not valid");
        }
    }

}
