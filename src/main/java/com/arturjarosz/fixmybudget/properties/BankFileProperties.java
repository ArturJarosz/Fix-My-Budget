package com.arturjarosz.fixmybudget.properties;

import java.util.List;

public record BankFileProperties(List<HeaderProperties> headers, int skipFirstLines, CsvDelimiter delimiter,
                                 CsvDelimiter trailingCharacter, boolean hasHeaders, int skipLastValues,
                                 String dateFormat, boolean shouldCleanRecipientSender, String charsetName) {
}
