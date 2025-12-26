package com.arturjarosz.fixmybudget.category.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@SequenceGenerator(name = "category_requirement_value_sequence_generator",
        sequenceName = "category_requirement_value_sequence", allocationSize = 1)
@Table(name = "CATEGORY_REQUIREMENT_VALUE")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CategoryRequirementValue {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_requirement_value_sequence_generator")
    private Long id;

    @Column(name = "REQUIREMENT_VALUE")
    private String value;
}
