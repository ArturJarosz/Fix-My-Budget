package com.arturjarosz.fixmybudget.properties;

import java.util.List;

public record CategoryRequirementsProperties(FieldType fieldType, MatchType matchType, List<String> values) {
}
