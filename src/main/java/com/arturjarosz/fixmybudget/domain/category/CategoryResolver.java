package com.arturjarosz.fixmybudget.domain.category;

import com.arturjarosz.fixmybudget.application.Bank;
import com.arturjarosz.fixmybudget.domain.field.FieldProvider;
import com.arturjarosz.fixmybudget.domain.model.BankTransaction;
import com.arturjarosz.fixmybudget.properties.CategoryEntryProperties;
import com.arturjarosz.fixmybudget.properties.CategoryProperties;
import com.arturjarosz.fixmybudget.properties.CategoryRequirementsProperties;
import com.arturjarosz.fixmybudget.properties.FieldType;
import com.arturjarosz.fixmybudget.properties.MatchType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CategoryResolver {
    private static final String UNCATEGORIZED = "UNCATEGORIZED";

    private final CategoryProperties categoryProperties;
    private final Map<MatchType, RequirementChecker> checkerByType;
    private final Map<FieldType, FieldProvider> fieldProviderByFieldType;

    public CategoryResolver(CategoryProperties categoryProperties, List<RequirementChecker> checkers,
            List<FieldProvider> fieldProviders) {
        this.categoryProperties = categoryProperties;
        this.checkerByType = checkers.stream()
                .collect(Collectors.toMap(RequirementChecker::getMatchType, checker -> checker));
        this.fieldProviderByFieldType = fieldProviders.stream()
                .collect(Collectors.toMap(FieldProvider::getFieldType, Function.identity()));
    }

    public void enrichWithCategories(List<BankTransaction> transactions, Bank bank) {
        for (BankTransaction bankTransaction : transactions) {
            var category = resolveCategory(bankTransaction, bank);
            bankTransaction.setCategory(category);

        }
    }

    String resolveCategory(BankTransaction bankTransaction, Bank bank) {
        var categoryMappings = categoryProperties.mapping();
        String resolvedCategory = UNCATEGORIZED;
        for (CategoryEntryProperties categoryMapping : categoryMappings.get(bank)) {
            var requirementsMet = true;
            for (CategoryRequirementsProperties requirement : categoryMapping.requirements()) {
                var checker = checkerByType.get(requirement.matchType());
                var fieldToEvaluate = fieldProviderByFieldType.get(requirement.fieldType())
                        .getFieldToEvaluate(bankTransaction);
                requirementsMet = checker.meetsRequirements(fieldToEvaluate, requirement);
                if (!requirementsMet) {
                    break;
                }
            }
            if (requirementsMet) {
                resolvedCategory = categoryMapping.name();
                break;
            }

        }
        return resolvedCategory;
    }

}
