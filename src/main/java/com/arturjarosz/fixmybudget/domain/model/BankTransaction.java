package com.arturjarosz.fixmybudget.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "bank_transaction_sequence_generator", sequenceName = "bank_transaction_sequence", allocationSize = 1)
@Table(name = "BANK_TRANSACTION")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_transaction_sequence_generator")
    private Long id;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false)
    private TransactionType transactionType;

    @Column(name = "AMOUNT", precision = 5, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "DETAILS")
    private String details;

    @Column(name = "RECIPIENT_NAME")
    private String recipientName;

    @Column(name = "SENDER_NAME")
    private String senderName;

    @Column(name = "SENDER_ACCOUNT")
    private String senderAccount;

    @Column(name = "RECIPIENT_ACCOUNT")
    private String recipientAccount;
}
