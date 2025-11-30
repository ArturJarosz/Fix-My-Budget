package com.arturjarosz.fixmybudget.rest;

import com.arturjarosz.fixmybudget.application.Bank;
import com.arturjarosz.fixmybudget.application.CsvService;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/csv")
public class CsvController {
    private final CsvService csvService;

    @PostMapping("/upload")
    ResponseEntity<AnalyzedStatementDto> readCsv(@RequestParam("file") MultipartFile file, @NonNull  @RequestParam("bank") Bank bank) {
        return ResponseEntity.ok(csvService.readCsv(file, bank));
    }

}
