package com.arturjarosz.fixmybudget.configuration.dto;

import com.arturjarosz.fixmybudget.properties.FieldType;
import com.arturjarosz.fixmybudget.properties.MatchType;
import lombok.Builder;

import java.util.List;

@Builder
public record ConfigurationResponse(List<String> banks, List<FieldType> fieldTypes, List<MatchType> matchTypes) {
}
