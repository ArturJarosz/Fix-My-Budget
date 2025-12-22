package com.arturjarosz.fixmybudget.field;

import com.arturjarosz.fixmybudget.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;
import org.springframework.stereotype.Component;

@Component
public class RecipientNameProvider implements FieldProvider {
    @Override
    public void enrichTransaction(BankTransaction transaction, String value) {
        transaction.setRecipientName(value);
    }

    @Override
    public FieldType getFieldType() {
        return FieldType.RECIPIENT_NAME;
    }

    @Override
    public FieldToEvaluate getFieldToEvaluate(BankTransaction transaction) {
        return FieldToEvaluate.builder()
                .fieldType(getFieldType())
                .textValue(transaction.getRecipientName())
                .build();
    }
}
