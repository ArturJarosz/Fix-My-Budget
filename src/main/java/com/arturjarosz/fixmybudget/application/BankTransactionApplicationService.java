package com.arturjarosz.fixmybudget.application;

import com.arturjarosz.fixmybudget.domain.BankTransactionDomainService;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class BankTransactionApplicationService {
    private final FileValidator fileValidator;
    private final BankTransactionDomainService bankTransactionDomainService;

    public AnalyzedStatementDto processCsv(MultipartFile file, Bank bank, String source) {
        fileValidator.checkIfFileIsNotEmpty(file);
        fileValidator.checkIfFileIsCsv(file);

        return bankTransactionDomainService.processCsv(file, bank, source);
    }

    public AnalyzedStatementDto calculateCategories(Bank bank) {
        return bankTransactionDomainService.calculateCategories(bank);
    }

    @Transactional
    public OverrideCategoryDto overrideCategory(Long bankTransactionId, OverrideCategoryDto overrideCategoryDto) {
        var updatedBankTransaction =
                bankTransactionDomainService.overrideCategory(bankTransactionId, overrideCategoryDto);
        return new OverrideCategoryDto(bankTransactionId, updatedBankTransaction.getCategory());
    }
}
