package com.arturjarosz.fixmybudget.transaction;

import com.arturjarosz.fixmybudget.category.model.Category;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.csv.validator.FileValidator;
import com.arturjarosz.fixmybudget.dto.OverrideCategoryDto;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import com.arturjarosz.fixmybudget.transaction.dto.TransactionsSummary;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class BankTransactionApplicationService {
    private final FileValidator fileValidator;
    private final BankTransactionDomainService bankTransactionDomainService;

    public AnalyzedStatementDto processCsv(MultipartFile file, Bank bank, String source) {
        log.info("Loading CSV file for bank {} from source {}.", bank, source);
        fileValidator.checkIfFileIsNotEmpty(file);
        fileValidator.checkIfFileIsCsv(file);

        return bankTransactionDomainService.processCsv(file, bank, source);
    }

    public AnalyzedStatementDto calculateCategories(Bank bank) {
        log.info("Calculating categories for bank {}.", bank);
        return bankTransactionDomainService.calculateCategories(bank);
    }

    @Transactional
    public BankTransaction overrideCategory(Long bankTransactionId, OverrideCategoryDto overrideCategoryDto) {
        log.info("Overriding category for bank transaction {}.", bankTransactionId);
        if (overrideCategoryDto.isOverride()){
            return bankTransactionDomainService.overrideCategory(bankTransactionId, overrideCategoryDto);
        }
        return bankTransactionDomainService.clearOverrideCategory(bankTransactionId);
    }

    public TransactionsSummary getTransactionsSummary() {
        log.info("Getting all transactions summary.");
        return bankTransactionDomainService.getTransactionsSummary();
    }

    public void cleanTransactionsOverriddenCategory(Category category) {
        this.bankTransactionDomainService.cleanTransactionsOverriddenCategory(category);
    }
}
