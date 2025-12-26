package com.arturjarosz.fixmybudget.category;

import com.arturjarosz.fixmybudget.category.model.Category;
import com.arturjarosz.fixmybudget.category.model.CategoryRequirement;
import com.arturjarosz.fixmybudget.category.repository.CategoryRepository;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.field.FieldProvider;
import com.arturjarosz.fixmybudget.properties.FieldType;
import com.arturjarosz.fixmybudget.properties.MatchType;
import com.arturjarosz.fixmybudget.transaction.model.BankTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CategoryResolver {
    private static final String UNCATEGORIZED = "UNCATEGORIZED";

    private final Map<MatchType, RequirementChecker> checkerByType;
    private final Map<FieldType, FieldProvider> fieldProviderByFieldType;
    private final CategoryRepository categoryRepository;

    public CategoryResolver(List<RequirementChecker> checkers, List<FieldProvider> fieldProviders,
            CategoryRepository categoryRepository) {
        this.checkerByType = checkers.stream()
                .collect(Collectors.toMap(RequirementChecker::getMatchType, checker -> checker));
        this.fieldProviderByFieldType = fieldProviders.stream()
                .collect(Collectors.toMap(FieldProvider::getFieldType, Function.identity()));
        this.categoryRepository = categoryRepository;
    }

    public void enrichWithCategories(List<BankTransaction> transactions, Bank bank) {
        for (BankTransaction bankTransaction : transactions) {
            var category = resolveCategory(bankTransaction, bank);
            bankTransaction.setCategory(category);

        }
    }

    String resolveCategory(BankTransaction bankTransaction, Bank bank) {
        var categories = categoryRepository.findAll()
                .stream()
                .filter(c -> c.getBankName() == bank)
                .toList();
        String resolvedCategory = UNCATEGORIZED;
        for (Category category : categories) {
            var requirementsMet = true;
            for (CategoryRequirement requirement : category.getRequirements()) {
                var checker = checkerByType.get(requirement.getMatchType());
                var fieldToEvaluate = fieldProviderByFieldType.get(requirement.getFieldType())
                        .getFieldToEvaluate(bankTransaction);
                requirementsMet = checker.meetsRequirements(fieldToEvaluate, requirement);
                if (!requirementsMet) {
                    break;
                }
            }
            if (requirementsMet) {
                resolvedCategory = category.getName();
                break;
            }

        }
        return resolvedCategory;
    }

}
