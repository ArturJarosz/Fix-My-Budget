package com.arturjarosz.fixmybudget.transaction.mapper;

import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.field.FieldProvider;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.properties.BankFileProperties;
import com.arturjarosz.fixmybudget.properties.FieldType;
import com.arturjarosz.fixmybudget.properties.HeaderProperties;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import com.arturjarosz.fixmybudget.transaction.model.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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

    private static void cleanRecipientSender(BankFileProperties bankFileProperties, BankTransaction transaction) {
        if (bankFileProperties.shouldCleanRecipientSender()) {
            if (transaction.getTransactionType() == TransactionType.INCOME) {
                transaction.setRecipientName(null);
            } else {
                transaction.setSenderName(null);
            }
        }
    }

    public BankTransaction map(String[] rowFields, Bank bank, List<String> headers) {
        var bankFileProperties = accountStatementFileProperties.banks()
                .get(bank);

        if (bankFileProperties.skipLastValues() > 0) {
            rowFields = Arrays.stream(rowFields)
                    .limit(rowFields.length - 4)
                    .toArray(String[]::new);
        }
        if (!(rowFields.length == accountStatementFileProperties.banks()
                .get(bank)
                .headers()
                .size())) {
            log.error("Row length is not equal to expected, skipping. Row: {}", (Object) rowFields);
            return null;
        }
        var transaction = new BankTransaction();
        for (int i = 0; i < rowFields.length; i++) {
            var header = accountStatementFileProperties.banks()
                    .get(bank)
                    .headers()
                    .get(i);
            var fieldTypes = header.types();
            String fallbackValue = getFallbackValue(rowFields, headers, header, bankFileProperties);
            if (fieldTypes == null || fieldTypes.isEmpty()) {
                continue;
            }
            var trimmedField = trimTrailingCharacter(rowFields[i], bankFileProperties.trailingCharacter()
                    .getCharacter());
            for (FieldType fieldType : fieldTypes) {
                var provider = fieldProviderByFieldType.get(fieldType);
                if (provider != null) {
                    provider.enrichTransaction(transaction, trimmedField, fallbackValue, bank);
                }
            }
        }
        cleanRecipientSender(bankFileProperties, transaction);

        return transaction;
    }

    private @Nullable String getFallbackValue(String[] rowFields, List<String> headers,
            HeaderProperties header, BankFileProperties bankFileProperties) {
        String fallbackValue = null;
        if (header.fallbackName() != null) {
            var fallbackValueIndex = getIndexOfFallbackHeader(header.fallbackName(), headers);
            if (fallbackValueIndex != -1) {
                fallbackValue = trimTrailingCharacter(rowFields[fallbackValueIndex],
                        bankFileProperties.trailingCharacter()
                                .getCharacter());
            }
        }
        return fallbackValue;
    }

    private int getIndexOfFallbackHeader(String fallbackName, List<String> headers) {
        var index = -1;
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i)
                    .equals(fallbackName)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private String trimTrailingCharacter(String str, char c) {
        while (str.endsWith(String.valueOf(c))) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }


}
