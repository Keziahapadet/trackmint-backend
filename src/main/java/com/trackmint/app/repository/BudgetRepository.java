package com.trackmint.app.repository;

import com.trackmint.app.entity.Budget;
import com.trackmint.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserOrderByCategoryAsc(User user);

    List<Budget> findByUserAndMonthAndYearOrderByCategoryAsc(User user, Integer month, Integer year);

    Optional<Budget> findByUserAndCategoryAndMonthAndYear(User user, String category, Integer month, Integer year);

    @Query("SELECT SUM(b.amount) FROM Budget b WHERE b.user = :user AND b.month = :month AND b.year = :year")
    Double getTotalBudgetByUserAndMonth(@Param("user") User user, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT SUM(b.spent) FROM Budget b WHERE b.user = :user AND b.month = :month AND b.year = :year")
    Double getTotalSpentByUserAndMonth(@Param("user") User user, @Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.spent > b.amount")
    List<Budget> findOverBudget(@Param("user") User user);

    @Query("SELECT b FROM Budget b WHERE b.user = :user AND (b.spent / b.amount) >= 0.8")
    List<Budget> findNearLimitBudgets(@Param("user") User user);

    boolean existsByUserAndCategoryAndMonthAndYear(User user, String category, Integer month, Integer year);
}