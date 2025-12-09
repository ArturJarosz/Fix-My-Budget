package com.arturjarosz.fixmybudget.application;

import com.arturjarosz.fixmybudget.domain.CsvDomainService;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class CsvApplicationService {
    private final FileValidator fileValidator;
    private final CsvDomainService csvDomainService;

    public AnalyzedStatementDto processCsv(MultipartFile file, Bank bank, String source) {
        fileValidator.checkIfFileIsNotEmpty(file);
        fileValidator.checkIfFileIsCsv(file);

        return csvDomainService.readCsv(file, bank, source);
    }

    public AnalyzedStatementDto calculateCategories(Bank bank) {
        return csvDomainService.calculateCategories(bank);
    }
}
