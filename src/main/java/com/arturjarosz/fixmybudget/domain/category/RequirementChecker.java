package com.arturjarosz.fixmybudget.domain.category;

import com.arturjarosz.fixmybudget.properties.CategoryRequirementsProperties;
import com.arturjarosz.fixmybudget.properties.MatchType;

import java.util.List;

public interface RequirementChecker {
    boolean meetsRequirements(FieldToEvaluate fieldToEvaluate, CategoryRequirementsProperties requirements);

    MatchType getMatchType();

    List<Class> getSupportedTypes();
}
