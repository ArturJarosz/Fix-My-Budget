package com.arturjarosz.fixmybudget.domain.validator;

import com.arturjarosz.fixmybudget.application.Bank;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.properties.HeaderProperties;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CsvValidator {
    private final AccountStatementFileProperties accountStatementFileProperties;

    public void checkFileHeaders(MultipartFile file, Bank bank) {
        var bankFileProperties = accountStatementFileProperties.banks()
                .get(bank);

        try (Reader reader = new InputStreamReader(file.getInputStream()); CSVReader csvReader = new CSVReaderBuilder(
                reader).withSkipLines(bankFileProperties.skipLines())
                .withCSVParser(new CSVParserBuilder().withSeparator(bankFileProperties.delimiter()
                                .getCharacter())
                        .build())
                .build()) {
            var headers = Arrays.stream(csvReader.readNext())
                    .map(header -> trimTrailingCharacter(header, bankFileProperties.trailingCharacter()
                            .getCharacter()))
                    .toList();
            if (headers.isEmpty()) {
                throw new IllegalArgumentException("CSV headers are empty");
            }

            var configuredFileHeaders = bankFileProperties.headers()
                    .stream()
                    .map(HeaderProperties::name)
                    .toList();
            validateHeaders(headers, configuredFileHeaders);

        } catch (IOException e) {
            throw new IllegalArgumentException("File is not csv or cannot be read");
        } catch (CsvException e) {
            throw new IllegalArgumentException("CSV file is not valid");
        }
    }

    private void validateHeaders(List<String> fileHeaders, List<String> expectedHeaders) {
        log.debug("Validating headers: {}", fileHeaders);
        log.debug("Expected headers: {}", expectedHeaders);
        if (fileHeaders.size() != expectedHeaders.size()) {
            throw new IllegalArgumentException("Numbers of columns in csv file is not equal to expected");
        }
        for (int i = 0; i < fileHeaders.size(); i++) {
            if (!fileHeaders.get(i)
                    .trim()
                    .equals(expectedHeaders.get(i))) {
                throw new IllegalArgumentException(
                        "Column %d in csv file is not equal to expected. Expected value: %s, value: %s.".formatted(
                                i + 1, expectedHeaders.get(i), fileHeaders.get(i)));
            }
        }
    }

    private String trimTrailingCharacter(String str, char delimiterChar) {
        while (str.endsWith(String.valueOf(delimiterChar))) {
            str = str.substring(0, str.length() - 1);
        }

        int index = str.length() - 1;
        while (str.charAt(index) == delimiterChar) {
            index--;
        }
        return str.substring(0, index + 1);
    }
}
