package com.arturjarosz.fixmybudget.properties;

import java.util.List;

public record BankFileProperties(List<HeaderProperties> headers, int skipLines, CsvDelimiter delimiter,
                                 boolean hasHeaders) {
}
