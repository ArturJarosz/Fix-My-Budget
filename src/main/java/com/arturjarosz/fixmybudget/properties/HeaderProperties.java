package com.arturjarosz.fixmybudget.properties;

import java.util.List;

public record HeaderProperties(String name, List<FieldType> types) {
}
