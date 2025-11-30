package com.arturjarosz.fixmybudget.application;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Component
public class FileValidator {
    // path passed validators
    public void checkIfFileExists(String filePath) {
        if (!Files.exists(Path.of(filePath))) {
            throw new IllegalArgumentException("File with path: %s does not exist".formatted(filePath));
        }
    }

    // file
    public void checkIfFileIsNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    public void checkIfFileIsCsv(MultipartFile file) {
        if (!Objects.requireNonNull(file.getOriginalFilename())
                .endsWith("csv") || !Objects.equals(file.getContentType(), "text/csv")) {
            throw new IllegalArgumentException("File is not csv");
        }
    }
}
