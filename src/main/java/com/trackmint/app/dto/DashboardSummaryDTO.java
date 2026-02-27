package com.trackmint.app.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardSummaryDTO {
    private Double totalBalance;
    private Double totalIncome;
    private Double totalExpenses;
    private Double totalSavings;
    private List<TransactionResponseDTO> recentTransactions;
    private Map<String, Double> categorySpending;
    private Map<String, Double> weeklySpending;
}