package com.arturjarosz.fixmybudget.domain.field;

import com.arturjarosz.fixmybudget.domain.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;
import org.springframework.stereotype.Component;

@Component
public class RecipientAccount implements FieldProvider {
    @Override
    public void enrichTransaction(BankTransaction transaction, String value) {
        transaction.setRecipientAccount(value);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.RECIPIENT_ACCOUNT;
    }

    @Override
    public FieldToEvaluate getFieldToEvaluate(BankTransaction transaction) {
        return FieldToEvaluate.builder()
                .fieldType(getFieldType())
                .textValue(transaction.getRecipientAccount())
                .build();
    }
}
