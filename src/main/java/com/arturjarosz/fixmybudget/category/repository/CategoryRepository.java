package com.arturjarosz.fixmybudget.category.repository;

import com.arturjarosz.fixmybudget.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
