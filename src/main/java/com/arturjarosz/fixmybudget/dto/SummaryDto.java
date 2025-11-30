package com.arturjarosz.fixmybudget.dto;

import java.math.BigDecimal;

public record SummaryDto(int transactionsCount, BigDecimal sum) {
}
