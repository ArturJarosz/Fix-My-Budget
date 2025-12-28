package com.arturjarosz.fixmybudget.category.rest;

import com.arturjarosz.fixmybudget.category.CategoryService;
import com.arturjarosz.fixmybudget.category.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    @Transactional
    @GetMapping("/download")
    ResponseEntity<byte[]> downloadConfiguration() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"category-properties.json\"");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(this.categoryService.getCategoriesAsFile());
    }

    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadConfiguration(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .build();
        }

        categoryService.saveCategories(file);
        return ResponseEntity.ok()
                .build();
    }

    @GetMapping()
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok()
                .body(this.categoryService.getCategories());
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(this.categoryService.createCategory(category));
    }
}


