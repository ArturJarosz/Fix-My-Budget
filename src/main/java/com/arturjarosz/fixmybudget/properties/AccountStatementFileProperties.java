package com.arturjarosz.fixmybudget.properties;

import com.arturjarosz.fixmybudget.dto.Bank;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "account-statement")
public record AccountStatementFileProperties(Map<Bank, BankFileProperties> banks) {
}
