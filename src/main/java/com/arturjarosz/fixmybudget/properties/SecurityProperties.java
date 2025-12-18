package com.arturjarosz.fixmybudget.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix =  "security")
public record SecurityProperties(List<String>allowedOrigins) {
}
