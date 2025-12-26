package com.arturjarosz.fixmybudget.category;

import com.arturjarosz.fixmybudget.category.model.CategoryRequirement;
import com.arturjarosz.fixmybudget.category.model.CategoryRequirementValue;
import com.arturjarosz.fixmybudget.properties.MatchType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EqualsRequirementCheck implements RequirementChecker {
    @Override
    public boolean meetsRequirements(FieldToEvaluate fieldToEvaluate, CategoryRequirement requirement) {
        var contains = false;
        var values = requirement.getValues()
                .stream()
                .map(CategoryRequirementValue::getValue)
                .toList();
        for (String value : values) {
            if (fieldToEvaluate.textValue()
                    .equalsIgnoreCase(value)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    @Override
    public MatchType getMatchType() {
        return MatchType.EQUALS;
    }

    @Override
    public List<Class> getSupportedTypes() {
        return List.of(String.class);
    }
}
