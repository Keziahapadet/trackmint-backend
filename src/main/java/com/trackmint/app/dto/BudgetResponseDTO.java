package com.trackmint.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BudgetResponseDTO {
    private Long id;
    private String category;
    private Double amount;
    private Double spent;
    private Double remaining;
    private Integer percentage;
    private String period;
    private Integer month;
    private Integer year;
    private String status; // on-track, warning, exceeded
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}