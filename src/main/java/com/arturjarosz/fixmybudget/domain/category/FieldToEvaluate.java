package com.arturjarosz.fixmybudget.domain.category;

import com.arturjarosz.fixmybudget.domain.model.TransactionType;
import com.arturjarosz.fixmybudget.properties.FieldType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record FieldToEvaluate(String textValue, BigDecimal numericValue, LocalDate date,
                              TransactionType transactionType, FieldType fieldType) {
}
