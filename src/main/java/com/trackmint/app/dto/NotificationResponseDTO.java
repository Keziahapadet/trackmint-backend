package com.trackmint.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;
    private String link;
    private String timeAgo;
    private String icon;
    private String color;

    // For budget notifications
    private String category;
    private Double percentage;
    private Double amount;
    private Double spent;
    private Double remaining;
}