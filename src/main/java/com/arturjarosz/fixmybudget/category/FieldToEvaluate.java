package com.arturjarosz.fixmybudget.category;

import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import com.arturjarosz.fixmybudget.properties.FieldType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record FieldToEvaluate(String textValue, BigDecimal numericValue, LocalDate date,
                              TransactionType transactionType, FieldType fieldType) {
}
