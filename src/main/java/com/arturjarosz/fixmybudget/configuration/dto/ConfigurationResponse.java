package com.arturjarosz.fixmybudget.configuration.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ConfigurationResponse(List<String> banks) {
}
