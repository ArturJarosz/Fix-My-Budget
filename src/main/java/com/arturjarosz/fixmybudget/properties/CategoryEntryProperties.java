package com.arturjarosz.fixmybudget.properties;

import java.util.List;

public record CategoryEntryProperties(String name, List<CategoryRequirementsProperties> requirements) {
}
