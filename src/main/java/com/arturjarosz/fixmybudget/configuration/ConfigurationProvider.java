package com.arturjarosz.fixmybudget.configuration;

import com.arturjarosz.fixmybudget.configuration.dto.ConfigurationResponse;
import com.arturjarosz.fixmybudget.properties.AccountStatementFileProperties;
import com.arturjarosz.fixmybudget.properties.FieldType;
import com.arturjarosz.fixmybudget.properties.MatchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfigurationProvider {
    private final AccountStatementFileProperties accountStatementFileProperties;

    public ConfigurationResponse getConfigurationResponse() {
        var banks = accountStatementFileProperties.banks()
                .keySet()
                .stream()
                .map(String::valueOf)
                .toList();
        var fieldTypes = Arrays.stream(FieldType.values())
                .toList();
        var matchTypes = Arrays.stream(MatchType.values())
                .toList();
        return ConfigurationResponse.builder()
                .banks(banks)
                .fieldTypes(fieldTypes)
                .matchTypes(matchTypes)
                .build();
    }

}
