package com.arturjarosz.fixmybudget.properties;

import lombok.Getter;

@Getter
public enum CsvDelimiter {
    COMA(','), SEMICOLON(';');

    private final char character;

    CsvDelimiter(char character) {
        this.character = character;
    }
}
