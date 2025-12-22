package com.arturjarosz.fixmybudget.configuration;

import com.arturjarosz.fixmybudget.configuration.dto.ConfigurationResponse;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfigurationProvider {
    private final AccountStatementFileProperties accountStatementFileProperties;

    public ConfigurationResponse getConfigurationResponse() {
        return ConfigurationResponse.builder()
                .banks(accountStatementFileProperties.banks()
                        .keySet()
                        .stream()
                        .map(String::valueOf)
                        .toList())
                .build();
    }
}
