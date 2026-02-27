package com.trackmint.app.controller;

import com.trackmint.app.dto.CategoryRequestDTO;
import com.trackmint.app.dto.CategoryResponseDTO;
import com.trackmint.app.dto.CategorySummaryDTO;
import com.trackmint.app.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category API", description = "Manage transaction categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Get all categories for current user")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(categoryService.getUserCategories(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(userDetails.getUsername(), id));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get category with transaction details")
    public ResponseEntity<CategoryResponseDTO> getCategoryWithDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryWithDetails(userDetails.getUsername(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(userDetails.getUsername(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        categoryService.deleteCategory(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get category summary")
    public ResponseEntity<CategorySummaryDTO> getCategorySummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(categoryService.getCategorySummary(userDetails.getUsername()));
    }
}