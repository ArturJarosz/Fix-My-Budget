package com.arturjarosz.fixmybudget.category.data;

import com.arturjarosz.fixmybudget.category.model.Category;
import com.arturjarosz.fixmybudget.category.repository.CategoryRepository;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CategoryInitialData implements ApplicationRunner {
    private final CategoryRepository categoryRepository;
    private final AccountStatementFileProperties accountStatementFileProperties;

    public void run(ApplicationArguments args) {

        var banks = this.accountStatementFileProperties.banks();
        banks.keySet().forEach(bank -> {
            var uncategorizedCategory =new Category();
            uncategorizedCategory.setName("UNCATEGORIZED");
            uncategorizedCategory.setBankName(bank);
            uncategorizedCategory.setColor("#888888");
            uncategorizedCategory.setIgnoreInBank(false);
            uncategorizedCategory.setIgnoreInSummary(false);

            this.categoryRepository.save(uncategorizedCategory);
        });
    }
}
