package com.arturjarosz.fixmybudget.transaction.repository;

import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findAllByTransactionType(TransactionType transactionType);
}
