package com.trackmint.app.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CategorySummaryDTO {
    private Long totalCategories;
    private Double totalSpent;
    private String topCategory;
    private Double averagePerCategory;
    private List<CategoryResponseDTO> categories;
    private Map<String, Double> spendingByCategory;
}