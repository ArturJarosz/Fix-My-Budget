package com.arturjarosz.fixmybudget.properties;

import com.arturjarosz.fixmybudget.dto.Bank;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "category")
public record CategoryProperties(Map<Bank, List<CategoryEntryProperties>> mapping, String defaultColor) {
}
