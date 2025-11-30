package com.arturjarosz.fixmybudget.properties;

import lombok.Getter;

@Getter
public enum CsvDelimiter {
    COMA(','), SEMICOLON(';');

    private final char delimiter;

    CsvDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }
}
