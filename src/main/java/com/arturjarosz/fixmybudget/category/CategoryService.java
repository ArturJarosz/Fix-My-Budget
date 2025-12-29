package com.arturjarosz.fixmybudget.category;

import com.arturjarosz.fixmybudget.category.exception.CategoryAlreadyExistsException;
import com.arturjarosz.fixmybudget.category.model.Category;
import com.arturjarosz.fixmybudget.category.model.CategoryRequirement;
import com.arturjarosz.fixmybudget.category.model.CategoryRequirementValue;
import com.arturjarosz.fixmybudget.category.repository.CategoryRepository;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.properties.CategoryProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryProperties categoryProperties;

    private static Category mergeCategoryGroup(CategoryKey key, List<Category> groupCategories) {
        Category result = new Category();
        // New entity, do not keep existing IDs
        result.setId(null);
        result.setName(key.name());
        result.setBankName(key.bank());

        List<CategoryRequirement> mergedRequirements = new ArrayList<>();
        for (Category category : groupCategories) {
            if (category.getRequirements() == null) {
                continue;
            }
            for (CategoryRequirement req : category.getRequirements()) {
                CategoryRequirement copy = copyRequirementWithoutIds(req);
                mergedRequirements.add(copy);
            }
        }

        result.setRequirements(mergedRequirements);
        return result;
    }

    private static CategoryRequirement copyRequirementWithoutIds(CategoryRequirement original) {
        CategoryRequirement copy = new CategoryRequirement();
        copy.setId(null);
        copy.setFieldType(original.getFieldType());
        copy.setMatchType(original.getMatchType());

        if (original.getValues() != null) {
            List<CategoryRequirementValue> valuesCopy = original.getValues()
                    .stream()
                    .map(value -> {
                        CategoryRequirementValue v = new CategoryRequirementValue();
                        v.setId(null);
                        v.setValue(value.getValue());
                        return v;
                    })
                    .toList();
            copy.setValues(valuesCopy);
        }

        return copy;
    }

    public byte[] getCategoriesAsFile() {
        var categories = categoryRepository.findAll();
        try {
            return objectMapper.writeValueAsBytes(categories);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveCategories(MultipartFile file) {
        List<Category> importedCategories = null;
        try {
            importedCategories = objectMapper.readValue(file.getInputStream(), new TypeReference<List<Category>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        categoryRepository.deleteAll();

        importedCategories = mergeDuplicateCategories(importedCategories);

        // making sure that some color is set for each category

        importedCategories.forEach(category -> {
            if (category.getColor() == null || category.getColor().isEmpty()) {
                category.setColor(categoryProperties.defaultColor());
            }
        });

        categoryRepository.saveAll(importedCategories);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> mergeDuplicateCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        Map<CategoryKey, List<Category>> grouped = categories.stream()
                .collect(Collectors.groupingBy(c -> new CategoryKey(c.getName(), c.getBankName()), LinkedHashMap::new,
                        Collectors.toList()));

        List<Category> merged = new ArrayList<>();
        for (Map.Entry<CategoryKey, List<Category>> entry : grouped.entrySet()) {
            List<Category> groupCategories = entry.getValue();
            merged.add(mergeCategoryGroup(entry.getKey(), groupCategories));
        }

        return merged;
    }

    @Transactional
    public Category createCategory(Category category) {
        if (this.categoryRepository.existsByNameAndBankName(category.getName(), category.getBankName())) {
            throw new CategoryAlreadyExistsException(
                    "Category with name '" + category.getName() + "' and bank '" + category.getBankName() + "' already exists.");
        }
        return this.categoryRepository.save(category);
    }

    private record CategoryKey(String name, Bank bank) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CategoryKey that)) return false;
            return Objects.equals(name, that.name) && bank == that.bank;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, bank);
        }
    }

}
