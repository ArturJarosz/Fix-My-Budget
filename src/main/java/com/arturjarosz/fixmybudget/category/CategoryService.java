package com.arturjarosz.fixmybudget.category;

import com.arturjarosz.fixmybudget.category.exception.CategoryAlreadyExistsException;
import com.arturjarosz.fixmybudget.category.model.Category;
import com.arturjarosz.fixmybudget.category.model.CategoryRequirement;
import com.arturjarosz.fixmybudget.category.model.CategoryRequirementValue;
import com.arturjarosz.fixmybudget.category.repository.CategoryRepository;
import com.arturjarosz.fixmybudget.dto.Bank;
import com.arturjarosz.fixmybudget.properties.CategoryProperties;
import com.arturjarosz.fixmybudget.transaction.BankTransactionApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final BankTransactionApplicationService bankTransactionApplicationService;

    private static Category mergeCategoryGroup(CategoryKey key, List<Category> groupCategories) {
        Category result = new Category();
        // New entity, do not keep existing IDs
        result.setId(null);
        result.setName(key.name());
        result.setBankName(key.bank());
        result.setColor(groupCategories.getFirst().getColor());

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
            if (category.getColor() == null || category.getColor()
                    .isEmpty()) {
                category.setColor(categoryProperties.defaultColor());
            }
        });

        categoryRepository.saveAll(importedCategories);
        bankTransactionApplicationService.calculateCategories(importedCategories.get(0).getBankName());
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> mergeDuplicateCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        Map<CategoryKey, List<Category>> categoriesByKey = categories.stream()
                .collect(Collectors.groupingBy(c -> new CategoryKey(c.getName(), c.getBankName()), LinkedHashMap::new,
                        Collectors.toList()));

        List<Category> merged = new ArrayList<>();
        for (Map.Entry<CategoryKey, List<Category>> entry : categoriesByKey.entrySet()) {
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
        this.bankTransactionApplicationService.calculateCategories(category.getBankName());
        return this.categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category incomingCategory) {
        var categoryToUpdate = this.categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category with id " + id + " not found"));

        // Update simple fields
        categoryToUpdate.setName(incomingCategory.getName());
        categoryToUpdate.setBankName(incomingCategory.getBankName());
        categoryToUpdate.setColor(incomingCategory.getColor());

        // Ensure requirements list is initialized
        if (categoryToUpdate.getRequirements() == null) {
            categoryToUpdate.setRequirements(new ArrayList<>());
        }

        mergeRequirements(categoryToUpdate.getRequirements(), incomingCategory.getRequirements());

        // Optionally, enforce uniqueness
        if (this.categoryRepository.existsByNameAndBankName(categoryToUpdate.getName(), categoryToUpdate.getBankName())
                && !Objects.equals(categoryToUpdate.getId(), id)) {
            throw new CategoryAlreadyExistsException(
                    "Category with name '" + categoryToUpdate.getName()
                            + "' and bank '" + categoryToUpdate.getBankName()
                            + "' already exists.");
        }
        this.bankTransactionApplicationService.calculateCategories(incomingCategory.getBankName());
        return this.categoryRepository.save(categoryToUpdate);
    }

    private void mergeRequirements(List<CategoryRequirement> existingRequirements,
            List<CategoryRequirement> incomingRequirements) {

        if (incomingRequirements == null) {
            // Client sent no requirements at all -> clear all
            existingRequirements.clear();
            return;
        }

        // Index incoming requirements by ID (ignoring new ones with null ID)
        Map<Long, CategoryRequirement> incomingById = new HashMap<>();
        List<CategoryRequirement> newRequirements = new ArrayList<>();

        for (CategoryRequirement incoming : incomingRequirements) {
            if (incoming.getId() == null) {
                newRequirements.add(incoming);
            } else {
                incomingById.put(incoming.getId(), incoming);
            }
        }

        // 1. Update or remove existing requirements
        existingRequirements.removeIf(existing -> {
            Long existingId = existing.getId();
            CategoryRequirement incomingMatch = existingId != null ? incomingById.get(existingId) : null;

            if (incomingMatch == null) {
                // Not present in incoming -> remove it
                return true;
            }

            // Update fields
            existing.setFieldType(incomingMatch.getFieldType());
            existing.setMatchType(incomingMatch.getMatchType());

            // Ensure values list is initialized
            if (existing.getValues() == null) {
                existing.setValues(new ArrayList<>());
            }

            mergeRequirementValues(existing.getValues(), incomingMatch.getValues());
            return false;
        });

        // 2. Add new requirements (no ID)
        for (CategoryRequirement incomingNew : newRequirements) {
            CategoryRequirement newReq = new CategoryRequirement();
            newReq.setId(null);
            newReq.setFieldType(incomingNew.getFieldType());
            newReq.setMatchType(incomingNew.getMatchType());

            List<CategoryRequirementValue> newValues = new ArrayList<>();
            if (incomingNew.getValues() != null) {
                for (CategoryRequirementValue v : incomingNew.getValues()) {
                    CategoryRequirementValue newVal = new CategoryRequirementValue();
                    newVal.setId(null);
                    newVal.setValue(v.getValue());
                    newValues.add(newVal);
                }
            }
            newReq.setValues(newValues);

            existingRequirements.add(newReq);
        }
    }

    private void mergeRequirementValues(List<CategoryRequirementValue> existingValues,
            List<CategoryRequirementValue> incomingValues) {

        if (incomingValues == null) {
            existingValues.clear();
            return;
        }

        Map<Long, CategoryRequirementValue> incomingById = new HashMap<>();
        List<CategoryRequirementValue> newValues = new ArrayList<>();

        for (CategoryRequirementValue incoming : incomingValues) {
            if (incoming.getId() == null) {
                newValues.add(incoming);
            } else {
                incomingById.put(incoming.getId(), incoming);
            }
        }

        // Update or remove existing values
        existingValues.removeIf(existing -> {
            Long existingId = existing.getId();
            CategoryRequirementValue incomingMatch = existingId != null ? incomingById.get(existingId) : null;

            if (incomingMatch == null) {
                // Removed on client side
                return true;
            }

            existing.setValue(incomingMatch.getValue());
            return false;
        });

        // Add new values
        for (CategoryRequirementValue incomingNew : newValues) {
            CategoryRequirementValue newVal = new CategoryRequirementValue();
            newVal.setId(null);
            newVal.setValue(incomingNew.getValue());
            existingValues.add(newVal);
        }
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
