package com.arturjarosz.fixmybudget.transaction.dto;

import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Objects;

@Builder
public record CategorySummary(String name, int count, BigDecimal totalAmount, TransactionType type) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CategorySummary that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
