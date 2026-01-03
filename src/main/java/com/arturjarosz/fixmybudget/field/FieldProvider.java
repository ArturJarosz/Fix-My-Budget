package com.arturjarosz.fixmybudget.field;

import com.arturjarosz.fixmybudget.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;

public interface FieldProvider {

    void enrichTransaction(BankTransaction transaction, String value, String fallbackValue, Bank bank);

    FieldType getFieldType();

    FieldToEvaluate getFieldToEvaluate(BankTransaction transaction);

    default FieldToEvaluate getFallbackFieldToEvaluate(BankTransaction transaction) {
        throw new IllegalArgumentException("This method is not implemented for this provider");
    }

}
