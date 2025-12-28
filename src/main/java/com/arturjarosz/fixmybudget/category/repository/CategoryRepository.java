package com.arturjarosz.fixmybudget.category.repository;

import com.arturjarosz.fixmybudget.category.model.Category;
import com.arturjarosz.fixmybudget.dto.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndBankName(String name, Bank bankName);
}
