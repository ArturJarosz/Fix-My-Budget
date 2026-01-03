package com.arturjarosz.fixmybudget.csv.validator;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.properties.HeaderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CsvValidator {
    private final AccountStatementFileProperties accountStatementFileProperties;

    public void checkFileHeaders(List<String> headers, Bank bank) {
        var bankFileProperties = accountStatementFileProperties.banks()
                .get(bank);
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("CSV headers are empty");
        }

        var configuredFileHeaders = bankFileProperties.headers()
                .stream()
                .map(HeaderProperties::name)
                .toList();
        validateHeaders(headers, configuredFileHeaders);
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
}
