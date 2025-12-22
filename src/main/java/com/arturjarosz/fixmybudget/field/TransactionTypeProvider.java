package com.arturjarosz.fixmybudget.field;

import com.arturjarosz.fixmybudget.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import com.arturjarosz.fixmybudget.properties.FieldType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

@Component
public class TransactionTypeProvider implements FieldProvider {
    private static final char DECIMAL_SEPARATOR = ',';
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("PL"));

    @Override
    public void enrichTransaction(BankTransaction transaction, String value) {
        symbols.setDecimalSeparator(DECIMAL_SEPARATOR);
        var format = new DecimalFormat("0,00", symbols);
        format.setParseBigDecimal(true);
        try {
            var parsedValue = (BigDecimal) format.parse(value);
            var isNegative = parsedValue.compareTo(BigDecimal.ZERO) < 0;
            transaction.setTransactionType(isNegative ? TransactionType.EXPENSE : TransactionType.INCOME);
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse number", e);
        }
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.TRANSACTION_TYPE;
    }

    @Override
    public FieldToEvaluate getFieldToEvaluate(BankTransaction transaction) {
        return FieldToEvaluate.builder()
                .fieldType(getFieldType())
                .transactionType(transaction.getTransactionType())
                .build();
    }
}
