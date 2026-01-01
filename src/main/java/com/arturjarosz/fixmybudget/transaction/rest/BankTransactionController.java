package com.arturjarosz.fixmybudget.transaction.rest;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.transaction.BankTransactionApplicationService;
import com.arturjarosz.fixmybudget.dto.OverrideCategoryDto;
import com.arturjarosz.fixmybudget.dto.AnalyzedStatementDto;
import com.arturjarosz.fixmybudget.transaction.dto.RecalculateCategoriesTransactionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bank-transactions")
public class BankTransactionController {
    private final BankTransactionApplicationService bankTransactionApplicationService;

    @PostMapping("/upload")
    ResponseEntity<AnalyzedStatementDto> readCsv(@RequestParam("file") MultipartFile file,
            @NonNull @RequestParam("bank") Bank bank, @NonNull @RequestParam("source") String source) {
        return ResponseEntity.ok(bankTransactionApplicationService.processCsv(file, bank, source));
    }

    @PostMapping("/calculate-categories")
    ResponseEntity<AnalyzedStatementDto> calculateCategories(@RequestBody RecalculateCategoriesTransactionsDto recalculateRequest) {
        return ResponseEntity.ok(bankTransactionApplicationService.calculateCategories(recalculateRequest.bank()));
    }

    @PostMapping("/{bankTransactionId}/override-category")
    ResponseEntity<OverrideCategoryDto> overrideTransaction(@PathVariable("bankTransactionId") Long bankTransactionId,
            @RequestBody OverrideCategoryDto overrideCategoryDto) {
        return ResponseEntity.ok(bankTransactionApplicationService.overrideCategory(bankTransactionId, overrideCategoryDto));
    }
}
