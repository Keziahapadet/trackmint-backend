package com.trackmint.app.controller;

import com.trackmint.app.dto.DashboardSummaryDTO;
import com.trackmint.app.dto.TransactionRequestDTO;
import com.trackmint.app.dto.TransactionResponseDTO;
import com.trackmint.app.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction API", description = "Manage transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Add a new transaction")
    public ResponseEntity<TransactionResponseDTO> addTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(
                        userDetails.getUsername(), dto));
    }

    @GetMapping
    @Operation(summary = "Get all transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService
                .getAllTransactions(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionService
                .getTransactionById(userDetails.getUsername(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService
                .updateTransaction(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        transactionService.deleteTransaction(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type")
    public ResponseEntity<List<TransactionResponseDTO>> getByType(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String type) {
        return ResponseEntity.ok(transactionService
                .getTransactionsByType(userDetails.getUsername(), type));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get transactions by category")
    public ResponseEntity<List<TransactionResponseDTO>> getByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        return ResponseEntity.ok(transactionService
                .getTransactionsByCategory(userDetails.getUsername(), category));
    }

    @GetMapping("/range")
    @Operation(summary = "Get transactions by date range")
    public ResponseEntity<List<TransactionResponseDTO>> getByDateRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(transactionService
                .getTransactionsByDateRange(userDetails.getUsername(), start, end));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService
                .getDashboardSummary(userDetails.getUsername()));
    }
}