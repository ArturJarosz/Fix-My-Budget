package com.arturjarosz.fixmybudget.domain.field;

import com.arturjarosz.fixmybudget.domain.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class TransactionDateProvider implements FieldProvider {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu");


    @Override
    public void enrichTransaction(BankTransaction transaction, String value) {
        var date = LocalDate.parse(value, DATE_TIME_FORMATTER);
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
