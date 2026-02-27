package com.trackmint.app.repository;

import com.trackmint.app.entity.Category;
import com.trackmint.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserOrderByNameAsc(User user);

    Optional<Category> findByUserAndId(User user, Long id);

    Optional<Category> findByUserAndName(User user, String name);

    boolean existsByUserAndName(User user, String name);

    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.id IN " +
            "(SELECT DISTINCT t.categoryEntity.id FROM Transaction t WHERE t.user = :user)")
    List<Category> findCategoriesWithTransactions(@Param("user") User user);

    // FIXED: Use categoryEntity instead of category
    @Query("SELECT c, COALESCE(SUM(t.amount), 0) as totalSpent, COUNT(t) as transactionCount " +
            "FROM Category c LEFT JOIN Transaction t ON t.categoryEntity = c AND t.type = 'EXPENSE' " +
            "WHERE c.user = :user GROUP BY c")
    List<Object[]> findCategoriesWithSpending(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.categoryEntity.id = :categoryId AND t.user = :user AND t.type = 'EXPENSE'")
    Double getTotalSpentByCategory(@Param("user") User user, @Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(t) FROM Transaction t " +
            "WHERE t.categoryEntity.id = :categoryId AND t.user = :user")
    Long getTransactionCountByCategory(@Param("user") User user, @Param("categoryId") Long categoryId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE'")
    Double getTotalSpending(@Param("user") User user);

    @Query("SELECT c FROM Category c WHERE c.user = :user AND c NOT IN " +
            "(SELECT DISTINCT t.categoryEntity FROM Transaction t WHERE t.user = :user AND t.categoryEntity IS NOT NULL)")
    List<Category> findCategoriesWithNoTransactions(@Param("user") User user);
}