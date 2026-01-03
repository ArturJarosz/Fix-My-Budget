package com.arturjarosz.fixmybudget.field;

import com.arturjarosz.fixmybudget.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;
import org.springframework.stereotype.Component;

@Component
public class TransactionDetailsProvider implements FieldProvider {
    @Override
    public void enrichTransaction(BankTransaction transaction, String value, String fallbackValue, Bank bank) {
        transaction.setDetails(value);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.TRANSACTION_DETAILS;
    }

    @Override
    public FieldToEvaluate getFieldToEvaluate(BankTransaction transaction) {
        return FieldToEvaluate.builder()
                .fieldType(getFieldType())
                .textValue(transaction.getDetails())
                .build();
    }
}
