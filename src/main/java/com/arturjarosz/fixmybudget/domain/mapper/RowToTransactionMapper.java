package com.arturjarosz.fixmybudget.domain.mapper;

import com.arturjarosz.fixmybudget.application.Bank;
import com.arturjarosz.fixmybudget.domain.field.FieldProvider;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.properties.FieldType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RowToTransactionMapper {
    private final AccountStatementFileProperties accountStatementFileProperties;
    private final Map<FieldType, FieldProvider> fieldProviderByFieldType;

    public RowToTransactionMapper(AccountStatementFileProperties accountStatementFileProperties,
            List<FieldProvider> fieldProviders) {
        this.accountStatementFileProperties = accountStatementFileProperties;
        this.fieldProviderByFieldType = fieldProviders.stream()
                .collect(Collectors.toMap(FieldProvider::getFieldType, Function.identity()));
    }

    public BankTransaction map(String[] rowFields, Bank bank) {
        if (!(rowFields.length == accountStatementFileProperties.banks()
                .get(bank)
                .headers()
                .size())) {
            log.error("Row length is not equal to expected, skipping. Row: {}", (Object) rowFields);
            return null;
        }
        var transaction = new BankTransaction();
        for (int i = 0; i < rowFields.length; i++) {
            var fieldTypes = accountStatementFileProperties.banks()
                    .get(bank)
                    .headers()
                    .get(i)
                    .types();
            if (fieldTypes == null || fieldTypes.isEmpty()) {
                continue;
            }
            var trimmedFiled = trimTrailingCharacter(rowFields[i], ',');
            for (FieldType fieldType : fieldTypes) {
                var provider = fieldProviderByFieldType.get(fieldType);
                if (provider != null) {
                    provider.enrichTransaction(transaction, trimmedFiled);
                }
            }
        }

        return transaction;
    }

    private String trimTrailingCharacter(String str, char c) {
        while (str.endsWith(String.valueOf(c))) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }


}
