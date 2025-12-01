package com.arturjarosz.fixmybudget.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BankTransaction {
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String category;
    private String details;
    private String recipientName;
    private String senderName;
    private String senderAccount;
    private String recipientAccount;
}
