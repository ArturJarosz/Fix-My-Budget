package com.arturjarosz.fixmybudget.dto;

import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;

import java.math.BigDecimal;
import java.util.List;

public record CategorySummaryDto(BigDecimal sum, List<BankTransaction> transactions) {

}
