package com.arturjarosz.fixmybudget.category;

import com.arturjarosz.fixmybudget.category.model.CategoryRequirement;
import com.arturjarosz.fixmybudget.properties.CategoryRequirementsProperties;
import com.arturjarosz.fixmybudget.properties.MatchType;

import java.util.List;

public interface RequirementChecker {
    boolean meetsRequirements(FieldToEvaluate fieldToEvaluate, CategoryRequirement requirement);

    MatchType getMatchType();

    List<Class> getSupportedTypes();
}
