package com.arturjarosz.fixmybudget.field;

import com.arturjarosz.fixmybudget.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Component
public class TransactionDateProvider implements FieldProvider {
    private static final String DEFAULT_DATE_FORMAT = "dd-MM-uuuu";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    private final AccountStatementFileProperties accountStatementFileProperties;

    @Override
    public void enrichTransaction(BankTransaction transaction, String value, String fallbackValue, Bank bank) {
        var dateFormat = accountStatementFileProperties.banks()
                .get(bank)
                .dateFormat() == null ? DEFAULT_DATE_FORMAT : accountStatementFileProperties.banks()
                .get(bank)
                .dateFormat();

        var date = LocalDate.parse(value, DateTimeFormatter.ofPattern(dateFormat));
        transaction.setTransactionDate(date);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.TRANSACTION_DATE;
    }

    @Override
    public FieldToEvaluate getFieldToEvaluate(BankTransaction transaction) {
        return FieldToEvaluate.builder()
                .fieldType(getFieldType())
                .date(transaction.getTransactionDate())
                .build();
    }
}
