package com.arturjarosz.fixmybudget.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverrideCategoryDto {
    private Long bankTransactionId;
    private String category;
}
