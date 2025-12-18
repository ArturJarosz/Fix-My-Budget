package com.arturjarosz.fixmybudget.rest;

import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.domain.model.TransactionType;
import com.arturjarosz.fixmybudget.domain.repository.BankTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BankTransactionQueryController {
    private final BankTransactionRepository bankTransactionRepository;

    @QueryMapping
    public BankTransaction bankTransaction(@Argument Long id) {
        return bankTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BankTransaction with ID " + id + " not found"));
    }

    @QueryMapping
    public List<BankTransaction> allBankTransactions() {
        return bankTransactionRepository.findAll();
    }

    @MutationMapping
    public BankTransaction updateBankTransactionCategory(@Argument Long id, @Argument String category) {
        BankTransaction transaction = bankTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BankTransaction with ID " + id + " not found"));
        transaction.setCategory(category);
        return bankTransactionRepository.save(transaction);
    }

    @QueryMapping
    public List<BankTransaction> bankTransactionsByType(@Argument TransactionType type) {
        return bankTransactionRepository.findAllByTransactionType(type);
    }
}
