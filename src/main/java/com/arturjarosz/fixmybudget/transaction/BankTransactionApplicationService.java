package com.arturjarosz.fixmybudget.transaction;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.csv.validator.FileValidator;
import com.arturjarosz.fixmybudget.dto.OverrideCategoryDto;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
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
        log.info("Processing CSV file for bank {} from source {}.", bank, source);
        fileValidator.checkIfFileIsNotEmpty(file);
        fileValidator.checkIfFileIsCsv(file);

        return bankTransactionDomainService.processCsv(file, bank, source);
    }

    public AnalyzedStatementDto calculateCategories(Bank bank) {
        log.info("Calculating categories for bank {}.", bank);
        return bankTransactionDomainService.calculateCategories(bank);
    }

    @Transactional
    public OverrideCategoryDto overrideCategory(Long bankTransactionId, OverrideCategoryDto overrideCategoryDto) {
        log.info("Overriding category for bank transaction {}.", bankTransactionId);
        var updatedBankTransaction =
                bankTransactionDomainService.overrideCategory(bankTransactionId, overrideCategoryDto);
        return new OverrideCategoryDto(bankTransactionId, updatedBankTransaction.getCategory());
    }
}
