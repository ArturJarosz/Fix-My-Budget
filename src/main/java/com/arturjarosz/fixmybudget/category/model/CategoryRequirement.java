package com.arturjarosz.fixmybudget.category.model;

import com.arturjarosz.fixmybudget.properties.FieldType;
import com.arturjarosz.fixmybudget.properties.MatchType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@SequenceGenerator(name = "category_requirement_sequence_generator", sequenceName = "category_requirement_sequence",
        allocationSize = 1)
@Table(name = "CATEGORY_REQUIREMENT")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CategoryRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_requirement_sequence_generator")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "FIELD_TYPE")
    private FieldType fieldType;

    @Enumerated(EnumType.STRING)
    @Column(name = "MATCH_TYPE")
    private MatchType matchType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_REQUIREMENT_ID", nullable = false)
    List<CategoryRequirementValue> values = new ArrayList<>();
}
