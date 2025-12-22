package com.arturjarosz.fixmybudget.field;

import com.arturjarosz.fixmybudget.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;

public interface FieldProvider {

    void enrichTransaction(BankTransaction transaction, String value);

    FieldType getFieldType();

    FieldToEvaluate getFieldToEvaluate(BankTransaction transaction);

}
