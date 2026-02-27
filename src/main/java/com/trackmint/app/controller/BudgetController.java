package com.trackmint.app.controller;

import com.trackmint.app.dto.BudgetRequestDTO;
import com.trackmint.app.dto.BudgetResponseDTO;
import com.trackmint.app.dto.BudgetSummaryDTO;
import com.trackmint.app.dto.BudgetAlertDTO;
import com.trackmint.app.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budget API", description = "Manage budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "Get all budgets")
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(budgetService.getUserBudgets(userDetails.getUsername()));
    }

    @GetMapping("/month/{month}/{year}")
    @Operation(summary = "Get budgets by month and year")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer month,
            @PathVariable Integer year) {
        return ResponseEntity.ok(budgetService.getBudgetsByMonth(userDetails.getUsername(), month, year));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(userDetails.getUsername(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(userDetails.getUsername(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequestDTO dto) {
        return ResponseEntity.ok(budgetService.updateBudget(userDetails.getUsername(), id, dto));
    }

    @PatchMapping("/{id}/spent")
    @Operation(summary = "Update spent amount")
    public ResponseEntity<BudgetResponseDTO> updateSpent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, Double> request) {
        Double spent = request.get("spent");
        if (spent == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(budgetService.updateSpent(userDetails.getUsername(), id, spent));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        budgetService.deleteBudget(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get budget summary")
    public ResponseEntity<BudgetSummaryDTO> getBudgetSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(budgetService.getBudgetSummary(userDetails.getUsername()));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get budget alerts")
    public ResponseEntity<List<BudgetAlertDTO>> getBudgetAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(budgetService.getBudgetAlerts(userDetails.getUsername()));
    }
}