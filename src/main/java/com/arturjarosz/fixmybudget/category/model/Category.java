package com.arturjarosz.fixmybudget.category.model;


import com.arturjarosz.fixmybudget.dto.Bank;
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
import java.util.Objects;

@Entity
@SequenceGenerator(name = "category_sequence_generator", sequenceName = "category_sequence", allocationSize = 1)
@Table(name = "CATEGORY")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_sequence_generator")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "BANK_NAME", nullable = false)
    private Bank bankName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    List<CategoryRequirement> requirements = new ArrayList<>();

    @Column(name = "COLOR")
    private String color;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Category category)) return false;
        return Objects.equals(name, category.name) && bankName == category.bankName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bankName);
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", bankName=" + bankName +
                '}';
    }
}
