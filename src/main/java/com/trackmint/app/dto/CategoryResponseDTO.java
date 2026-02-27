package com.trackmint.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private Double totalSpent;
    private Long transactionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}