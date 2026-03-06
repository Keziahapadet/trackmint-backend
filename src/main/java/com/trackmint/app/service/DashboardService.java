package com.trackmint.app.service;

import com.trackmint.app.dto.DashboardSummaryDTO;
import com.trackmint.app.dto.TransactionResponseDTO;
import com.trackmint.app.entity.Transaction;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.TransactionRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public DashboardService(TransactionRepository transactionRepository,
                            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }



    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary(String email) {
        User user = findUserByEmail(email);

        DashboardSummaryDTO summary = new DashboardSummaryDTO();


        Double totalIncome = transactionRepository
                .sumAmountByUserAndType(user, "INCOME");
        Double totalExpenses = transactionRepository
                .sumAmountByUserAndType(user, "EXPENSE");

        totalIncome = totalIncome != null ? totalIncome : 0.0;
        totalExpenses = totalExpenses != null ? totalExpenses : 0.0;

        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setTotalBalance(totalIncome - totalExpenses);
        summary.setTotalSavings(totalIncome - totalExpenses);


        summary.setRecentTransactions(getRecentTransactions(user));

        summary.setCategorySpending(getCategorySpending(user));


        summary.setWeeklySpending(getWeeklySpending(user));

        return summary;
    }



    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private List<TransactionResponseDTO> getRecentTransactions(User user) {
        return transactionRepository.findTop5ByUserOrderByDateDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Map<String, Double> getCategorySpending(User user) {
        List<Object[]> categorySpendingRaw = transactionRepository
                .findSpendingByCategory(user);
        Map<String, Double> categorySpending = new HashMap<>();
        for (Object[] row : categorySpendingRaw) {
            categorySpending.put((String) row[0], (Double) row[1]);
        }
        return categorySpending;
    }

    private Map<String, Double> getWeeklySpending(User user) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> weeklyRaw = transactionRepository
                .findWeeklySpendingNative(user.getId(), weekAgo);

        Map<String, Double> weeklySpending = new HashMap<>();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            weeklySpending.put(day, 0.0);
        }
        for (Object[] row : weeklyRaw) {
            Integer dayOfWeek = ((Number) row[0]).intValue();
            Double amount = ((Number) row[1]).doubleValue();
            weeklySpending.put(getDayName(dayOfWeek), amount);
        }
        return weeklySpending;
    }

    private String getDayName(int dayOfWeek) {
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
        dto.setAmount(transaction.getType().equals("EXPENSE") ?
                -transaction.getAmount() : transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        return dto;
    }
}