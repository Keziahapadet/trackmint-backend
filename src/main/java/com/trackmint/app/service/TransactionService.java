package com.trackmint.app.service;

import com.trackmint.app.dto.DashboardSummaryDTO;
import com.trackmint.app.dto.TransactionRequestDTO;
import com.trackmint.app.dto.TransactionResponseDTO;
import com.trackmint.app.entity.Transaction;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.TransactionRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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



    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions(String email) {
        User user = findUserByEmail(email);
        return transactionRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(String email, Long id) {
        User user = findUserByEmail(email);
        Transaction transaction = findTransactionById(id);
        checkOwnership(transaction, user);
        return toResponseDTO(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByType(String email, String type) {
        User user = findUserByEmail(email);
        return transactionRepository.findByUserAndType(user, type.toUpperCase())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByCategory(String email, String category) {
        User user = findUserByEmail(email);
        return transactionRepository.findByUserAndCategory(user, category)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByDateRange(String email,
                                                                   LocalDateTime start,
                                                                   LocalDateTime end) {
        User user = findUserByEmail(email);
        return transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary(String email) {
        User user = findUserByEmail(email);

        Double totalIncome = transactionRepository.sumAmountByUserAndType(user, "INCOME");
        Double totalExpenses = transactionRepository.sumAmountByUserAndType(user, "EXPENSE");

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
        List<Object[]> categoryData = transactionRepository.findSpendingByCategory(user);
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



    @Transactional
    public TransactionResponseDTO addTransaction(String email, TransactionRequestDTO dto) {
        User user = findUserByEmail(email);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setDescription(dto.getDescription());
        transaction.setCategory(dto.getCategory());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType().toUpperCase());
        transaction.setDate(dto.getDate() != null ? dto.getDate() : LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);
        return toResponseDTO(saved);
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(String email, Long id,
                                                    TransactionRequestDTO dto) {
        User user = findUserByEmail(email);
        Transaction transaction = findTransactionById(id);
        checkOwnership(transaction, user);

        if (dto.getDescription() != null) transaction.setDescription(dto.getDescription());
        if (dto.getCategory() != null) transaction.setCategory(dto.getCategory());
        if (dto.getAmount() != null) transaction.setAmount(dto.getAmount());
        if (dto.getType() != null) transaction.setType(dto.getType().toUpperCase());
        if (dto.getDate() != null) transaction.setDate(dto.getDate());

        Transaction saved = transactionRepository.save(transaction);
        return toResponseDTO(saved);
    }

    @Transactional
    public void deleteTransaction(String email, Long id) {
        User user = findUserByEmail(email);
        Transaction transaction = findTransactionById(id);
        checkOwnership(transaction, user);
        transactionRepository.delete(transaction);
    }



    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    private void checkOwnership(Transaction transaction, User user) {
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
    }

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
}