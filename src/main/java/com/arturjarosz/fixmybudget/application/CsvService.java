package com.arturjarosz.fixmybudget.application;

import com.arturjarosz.fixmybudget.domain.CsvReaderService;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class CsvService {
    private final FileValidator fileValidator;
    private final CsvReaderService csvReader;

    public AnalyzedStatementDto readCsv(MultipartFile file, Bank bank) {
        fileValidator.checkIfFileIsNotEmpty(file);
        fileValidator.checkIfFileIsCsv(file);

        return csvReader.readCsv(file, bank);
    }
}
