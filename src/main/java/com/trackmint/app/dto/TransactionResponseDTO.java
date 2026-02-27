package com.trackmint.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long id;
    private String description;
    private String category;
    private Double amount;
    private String type;
    private LocalDateTime date;
}