package com.trackmint.app.service;

import com.trackmint.app.dto.DashboardSummaryDTO;
import com.trackmint.app.dto.TransactionResponseDTO;
import com.trackmint.app.entity.Transaction;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.TransactionRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public DashboardService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryDTO getDashboardSummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        Double totalIncome = transactionRepository.sumAmountByUserAndType(user, "INCOME");
        Double totalExpenses = transactionRepository.sumAmountByUserAndType(user, "EXPENSE");

        totalIncome = totalIncome != null ? totalIncome : 0.0;
        totalExpenses = totalExpenses != null ? totalExpenses : 0.0;

        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setTotalBalance(totalIncome - totalExpenses);
        summary.setTotalSavings(totalIncome - totalExpenses);

        List<Transaction> recentTransactions = transactionRepository.findTop5ByUserOrderByDateDesc(user);
        List<TransactionResponseDTO> recentDTOs = recentTransactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        summary.setRecentTransactions(recentDTOs);

        List<Object[]> categorySpendingRaw = transactionRepository.findSpendingByCategory(user);
        Map<String, Double> categorySpending = new HashMap<>();
        for (Object[] row : categorySpendingRaw) {
            categorySpending.put((String) row[0], (Double) row[1]);
        }
        summary.setCategorySpending(categorySpending);

        // FIXED: Use native query with user ID
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> weeklyRaw = transactionRepository.findWeeklySpendingNative(user.getId(), weekAgo);

        Map<String, Double> weeklySpending = new HashMap<>();
        // PostgreSQL DOW: 0=Sunday, 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            weeklySpending.put(day, 0.0);
        }

        for (Object[] row : weeklyRaw) {
            // PostgreSQL DOW returns 0-6 where 0=Sunday
            Integer dayOfWeek = ((Number) row[0]).intValue();
            Double amount = ((Number) row[1]).doubleValue();
            String dayName = getDayNamePostgres(dayOfWeek);
            weeklySpending.put(dayName, amount);
        }
        summary.setWeeklySpending(weeklySpending);

        return summary;
    }

    private String getDayNamePostgres(int dayOfWeek) {
        // PostgreSQL DOW: 0=Sunday, 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday
        switch (dayOfWeek) {
            case 0: return "Sun";
            case 1: return "Mon";
            case 2: return "Tue";
            case 3: return "Wed";
            case 4: return "Thu";
            case 5: return "Fri";
            case 6: return "Sat";
            default: return "Mon";
        }
    }

    private TransactionResponseDTO convertToDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setCategory(transaction.getCategory());
        dto.setAmount(transaction.getType().equals("EXPENSE") ? -transaction.getAmount() : transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        return dto;
    }
}