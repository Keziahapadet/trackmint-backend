package com.trackmint.app.service;

import com.trackmint.app.dto.*;
import com.trackmint.app.entity.Budget;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.BudgetRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
    }

    public List<BudgetResponseDTO> getUserBudgets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return budgetRepository.findByUserOrderByCategoryAsc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BudgetResponseDTO> getBudgetsByMonth(String email, Integer month, Integer year) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return budgetRepository.findByUserAndMonthAndYearOrderByCategoryAsc(user, month, year)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BudgetResponseDTO getBudgetById(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        return convertToDTO(budget);
    }

    @Transactional
    public BudgetResponseDTO createBudget(String email, BudgetRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        Integer month = dto.getMonth() != null ? dto.getMonth() : now.getMonthValue();
        Integer year = dto.getYear() != null ? dto.getYear() : now.getYear();

        // Check if budget already exists for this category and month
        if (budgetRepository.existsByUserAndCategoryAndMonthAndYear(user, dto.getCategory(), month, year)) {
            throw new RuntimeException("Budget already exists for this category in " + month + "/" + year);
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(dto.getCategory());
        budget.setAmount(dto.getAmount());
        budget.setSpent(dto.getSpent() != null ? dto.getSpent() : 0.0);
        budget.setPeriod(dto.getPeriod() != null ? dto.getPeriod() : "monthly");
        budget.setMonth(month);
        budget.setYear(year);

        Budget savedBudget = budgetRepository.save(budget);
        return convertToDTO(savedBudget);
    }

    @Transactional
    public BudgetResponseDTO updateBudget(String email, Long id, BudgetRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        if (dto.getCategory() != null) budget.setCategory(dto.getCategory());
        if (dto.getAmount() != null) budget.setAmount(dto.getAmount());
        if (dto.getSpent() != null) budget.setSpent(dto.getSpent());
        if (dto.getPeriod() != null) budget.setPeriod(dto.getPeriod());
        if (dto.getMonth() != null) budget.setMonth(dto.getMonth());
        if (dto.getYear() != null) budget.setYear(dto.getYear());

        Budget updatedBudget = budgetRepository.save(budget);
        return convertToDTO(updatedBudget);
    }

    @Transactional
    public BudgetResponseDTO updateSpent(String email, Long id, Double spent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        budget.setSpent(spent);
        Budget updatedBudget = budgetRepository.save(budget);
        return convertToDTO(updatedBudget);
    }

    @Transactional
    public void deleteBudget(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to budget");
        }

        budgetRepository.delete(budget);
    }

    public BudgetSummaryDTO getBudgetSummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        Integer currentMonth = now.getMonthValue();
        Integer currentYear = now.getYear();

        BudgetSummaryDTO summary = new BudgetSummaryDTO();

        Double totalBudget = budgetRepository.getTotalBudgetByUserAndMonth(user, currentMonth, currentYear);
        Double totalSpent = budgetRepository.getTotalSpentByUserAndMonth(user, currentMonth, currentYear);

        summary.setTotalBudget(totalBudget != null ? totalBudget : 0.0);
        summary.setTotalSpent(totalSpent != null ? totalSpent : 0.0);
        summary.setRemaining(summary.getTotalBudget() - summary.getTotalSpent());

        List<Budget> overBudget = budgetRepository.findOverBudget(user);
        summary.setOverBudgetCount((long) overBudget.size());

        Double overBudgetAmount = overBudget.stream()
                .mapToDouble(b -> b.getSpent() - b.getAmount())
                .sum();
        summary.setOverBudget(overBudgetAmount);

        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYearOrderByCategoryAsc(user, currentMonth, currentYear);
        summary.setBudgetCount((long) budgets.size());

        List<BudgetResponseDTO> budgetDTOs = budgets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        summary.setBudgets(budgetDTOs);

        summary.setAlerts(getBudgetAlerts(user));

        return summary;
    }

    public List<BudgetAlertDTO> getBudgetAlerts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getBudgetAlerts(user);
    }

    private List<BudgetAlertDTO> getBudgetAlerts(User user) {
        List<BudgetAlertDTO> alerts = new ArrayList<>();

        // Check over budget
        List<Budget> overBudget = budgetRepository.findOverBudget(user);
        for (Budget budget : overBudget) {
            BudgetAlertDTO alert = new BudgetAlertDTO();
            alert.setCategory(budget.getCategory());
            alert.setType("EXCEEDED");
            alert.setAmount(budget.getAmount());
            alert.setSpent(budget.getSpent());
            alert.setRemaining(budget.getAmount() - budget.getSpent());

            // FIXED: Convert to Double
            Double percentage = (budget.getSpent() / budget.getAmount()) * 100;
            alert.setPercentage(percentage);

            alert.setMessage("You have exceeded your " + budget.getCategory() + " budget by ksh " +
                    String.format("%.2f", (budget.getSpent() - budget.getAmount())));
            alerts.add(alert);
        }

        // Check near limit (80%+)
        List<Budget> nearLimit = budgetRepository.findNearLimitBudgets(user);
        for (Budget budget : nearLimit) {
            if (budget.getSpent() <= budget.getAmount()) { // Not already over budget
                BudgetAlertDTO alert = new BudgetAlertDTO();
                alert.setCategory(budget.getCategory());
                alert.setType("WARNING");
                alert.setAmount(budget.getAmount());
                alert.setSpent(budget.getSpent());
                alert.setRemaining(budget.getAmount() - budget.getSpent());

                // FIXED: Convert to Double
                Double percentage = (budget.getSpent() / budget.getAmount()) * 100;
                alert.setPercentage(percentage);

                alert.setMessage("You have used " + percentage.intValue() + "% of your " +
                        budget.getCategory() + " budget");
                alerts.add(alert);
            }
        }

        return alerts;
    }
    private BudgetResponseDTO convertToDTO(Budget budget) {
        BudgetResponseDTO dto = new BudgetResponseDTO();
        dto.setId(budget.getId());
        dto.setCategory(budget.getCategory());
        dto.setAmount(budget.getAmount());
        dto.setSpent(budget.getSpent());
        dto.setRemaining(budget.getAmount() - budget.getSpent());

        int percentage = budget.getAmount() > 0 ?
                (int) ((budget.getSpent() / budget.getAmount()) * 100) : 0;
        dto.setPercentage(Math.min(percentage, 100));

        if (percentage >= 100) {
            dto.setStatus("exceeded");
        } else if (percentage >= 80) {
            dto.setStatus("warning");
        } else {
            dto.setStatus("on-track");
        }

        dto.setPeriod(budget.getPeriod());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());
        return dto;
    }
}