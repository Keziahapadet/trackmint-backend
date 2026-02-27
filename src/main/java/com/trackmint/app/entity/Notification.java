package com.trackmint.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    private String type; // BUDGET_ALERT, BUDGET_WARNING, BUDGET_EXCEEDED, LARGE_TRANSACTION, WEEKLY_SUMMARY, GOAL_ACHIEVED, SYSTEM

    private boolean isRead = false;

    private LocalDateTime createdAt;

    private String link; // Optional: link to relevant page (e.g., /budgets, /transactions)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}