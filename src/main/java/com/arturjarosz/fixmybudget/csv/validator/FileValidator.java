package com.arturjarosz.fixmybudget.csv.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
public class FileValidator {
    private static final String CSV_EXTENSION = "csv";
    private static final String CSV_CONTENT_TYPE = "text/csv";

    // file
    public void checkIfFileIsNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    public void checkIfFileIsCsv(MultipartFile file) {
        if (!Objects.requireNonNull(file.getOriginalFilename())
                .endsWith(CSV_EXTENSION) || !Objects.equals(file.getContentType(), CSV_CONTENT_TYPE)) {
            throw new IllegalArgumentException("File is not csv");
        }
    }
}
