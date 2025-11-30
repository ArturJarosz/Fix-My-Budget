package com.arturjarosz.fixmybudget.domain.field;

import com.arturjarosz.fixmybudget.domain.category.FieldToEvaluate;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.FieldType;

public interface FieldProvider {

    void enrichTransaction(BankTransaction transaction, String value);

    FieldType getFieldType();

    FieldToEvaluate getFieldToEvaluate(BankTransaction transaction);

}
