package com.arturjarosz.fixmybudget.transaction.dto;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record TransactionsSummary(Map<Bank, Map<TransactionType, List<CategorySummary>>> categorySummaryByTypeByBank,
                                  Map<TransactionType, List<CategorySummary>> allBanksCategorySummaries) {
}
