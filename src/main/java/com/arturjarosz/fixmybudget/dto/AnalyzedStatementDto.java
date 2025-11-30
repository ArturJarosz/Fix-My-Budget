package com.arturjarosz.fixmybudget.dto;

import lombok.Builder;

import java.util.Map;

@Builder

public record AnalyzedStatementDto(Map<String, SummaryDto> summary, Map<String, CategoryDto> transactionsByCategory) {
}

