package com.trackmint.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class BudgetSummaryDTO {
    private Double totalBudget;
    private Double totalSpent;
    private Double remaining;
    private Double overBudget;
    private Long budgetCount;
    private Long overBudgetCount;
    private List<BudgetResponseDTO> budgets;
    private List<BudgetAlertDTO> alerts;
}