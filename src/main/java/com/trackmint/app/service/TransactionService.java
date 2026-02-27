package com.trackmint.app.service;

import com.trackmint.app.dto.DashboardSummaryDTO;
import com.trackmint.app.dto.TransactionRequestDTO;
import com.trackmint.app.dto.TransactionResponseDTO;
import com.trackmint.app.entity.Transaction;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.TransactionRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // Convert Entity to DTO
    private TransactionResponseDTO toResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setDescription(transaction.getDescription());
        dto.setCategory(transaction.getCategory());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        return dto;
    }

    // Get user from email
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Add transaction
    public TransactionResponseDTO addTransaction(String email,
                                                 TransactionRequestDTO dto) {
        User user = getUserByEmail(email);
        Transaction transaction = new Transaction();
        transaction.setDescription(dto.getDescription());
        transaction.setCategory(dto.getCategory());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType().toUpperCase());
        transaction.setDate(dto.getDate() != null ? dto.getDate() : LocalDateTime.now());
        transaction.setUser(user);
        return toResponseDTO(transactionRepository.save(transaction));
    }

    // Get all transactions
    public List<TransactionResponseDTO> getAllTransactions(String email) {
        User user = getUserByEmail(email);
        return transactionRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get transaction by id
    public TransactionResponseDTO getTransactionById(String email, Long id) {
        User user = getUserByEmail(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
        return toResponseDTO(transaction);
    }

    // Update transaction ← NEW
    public TransactionResponseDTO updateTransaction(String email, Long id,
                                                    TransactionRequestDTO dto) {
        User user = getUserByEmail(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
        transaction.setDescription(dto.getDescription());
        transaction.setCategory(dto.getCategory());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType().toUpperCase());
        if (dto.getDate() != null) {
            transaction.setDate(dto.getDate());
        }
        return toResponseDTO(transactionRepository.save(transaction));
    }

    // Delete transaction
    public void deleteTransaction(String email, Long id) {
        User user = getUserByEmail(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
        transactionRepository.deleteById(id);
    }

    // Get by type ← NEW
    public List<TransactionResponseDTO> getTransactionsByType(String email,
                                                              String type) {
        User user = getUserByEmail(email);
        return transactionRepository.findByUserAndType(user, type.toUpperCase())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get by category ← NEW
    public List<TransactionResponseDTO> getTransactionsByCategory(String email,
                                                                  String category) {
        User user = getUserByEmail(email);
        return transactionRepository.findByUserAndCategory(user, category)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get by date range ← NEW
    public List<TransactionResponseDTO> getTransactionsByDateRange(String email,
                                                                   LocalDateTime start,
                                                                   LocalDateTime end) {
        User user = getUserByEmail(email);
        return transactionRepository
                .findByUserAndDateBetweenOrderByDateDesc(user, start, end)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Get dashboard summary
    public DashboardSummaryDTO getDashboardSummary(String email) {
        User user = getUserByEmail(email);

        Double totalIncome = transactionRepository
                .sumAmountByUserAndType(user, "INCOME");
        Double totalExpenses = transactionRepository
                .sumAmountByUserAndType(user, "EXPENSE");

        totalIncome = totalIncome != null ? totalIncome : 0.0;
        totalExpenses = totalExpenses != null ? totalExpenses : 0.0;

        Double totalBalance = totalIncome - totalExpenses;
        Double totalSavings = totalBalance > 0 ? totalBalance : 0.0;

        List<TransactionResponseDTO> recentTransactions = transactionRepository
                .findTop5ByUserOrderByDateDesc(user)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        Map<String, Double> categorySpending = new HashMap<>();
        List<Object[]> categoryData = transactionRepository
                .findSpendingByCategory(user);
        for (Object[] row : categoryData) {
            categorySpending.put((String) row[0], (Double) row[1]);
        }

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalBalance(totalBalance);
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setTotalSavings(totalSavings);
        summary.setRecentTransactions(recentTransactions);
        summary.setCategorySpending(categorySpending);

        return summary;
    }
}