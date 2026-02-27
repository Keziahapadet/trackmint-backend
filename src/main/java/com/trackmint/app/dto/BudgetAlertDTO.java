package com.trackmint.app.dto;

import lombok.Data;

@Data
public class BudgetAlertDTO {
    private String category;
    private String message;
    private String type; // WARNING, EXCEEDED
    private Double percentage;
    private Double amount;
    private Double spent;
    private Double remaining;
}