package com.trackmint.app.repository;

import com.trackmint.app.entity.Transaction;
import com.trackmint.app.entity.User;
import com.trackmint.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Basic user queries
    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findTop5ByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndType(User user, String type);

    // Keep the String version for backward compatibility
    List<Transaction> findByUserAndCategory(User user, String category);

    // Use categoryEntity for the Category object version
    List<Transaction> findByUserAndCategoryEntity(User user, Category category);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDateTime start, LocalDateTime end);

    // Sum queries
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    Double sumAmountByUserAndType(@Param("user") User user, @Param("type") String type);

    // Spending by category (using String for backward compatibility)
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE' GROUP BY t.category")
    List<Object[]> findSpendingByCategory(@Param("user") User user);

    // Get total spent for a specific category using Category entity
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.categoryEntity = :category AND t.type = 'EXPENSE'")
    Double getTotalSpentByCategory(@Param("user") User user, @Param("category") Category category);

    // Get transaction count for a specific category using Category entity
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.categoryEntity = :category")
    Long getTransactionCountByCategory(@Param("user") User user, @Param("category") Category category);

    // Get spending by category with Category objects
    @Query("SELECT t.categoryEntity, SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE' GROUP BY t.categoryEntity")
    List<Object[]> findSpendingByCategoryObject(@Param("user") User user);

    // Get all transactions for a specific category ordered by date using Category entity
    List<Transaction> findByUserAndCategoryEntityOrderByDateDesc(User user, Category category);

    // Get monthly spending for a specific category using Category entity
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.categoryEntity = :category AND t.type = 'EXPENSE' AND FUNCTION('MONTH', t.date) = :month AND FUNCTION('YEAR', t.date) = :year")
    Double getMonthlySpentByCategory(@Param("user") User user, @Param("category") Category category, @Param("month") int month, @Param("year") int year);

    // FIXED: Get categories with no transactions - using categoryEntity
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c NOT IN " +
            "(SELECT DISTINCT t.categoryEntity FROM Transaction t WHERE t.user = :user AND t.categoryEntity IS NOT NULL)")
    List<Category> findCategoriesWithNoTransactions(@Param("user") User user);

    // Weekly spending native query
    @Query(value = "SELECT EXTRACT(DOW FROM t.date) as day_of_week, COALESCE(SUM(t.amount), 0) " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId AND t.date >= :startDate " +
            "GROUP BY EXTRACT(DOW FROM t.date)",
            nativeQuery = true)
    List<Object[]> findWeeklySpendingNative(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}