package com.trackmint.app.service;

import com.trackmint.app.dto.CategoryRequestDTO;
import com.trackmint.app.dto.CategoryResponseDTO;
import com.trackmint.app.dto.CategorySummaryDTO;
import com.trackmint.app.entity.Category;
import com.trackmint.app.entity.User;
import com.trackmint.app.repository.CategoryRepository;
import com.trackmint.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryResponseDTO> getUserCategories(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> results = categoryRepository.findCategoriesWithSpending(user);
        List<CategoryResponseDTO> categories = new ArrayList<>();

        for (Object[] result : results) {
            Category category = (Category) result[0];
            Double totalSpent = (Double) result[1];
            Long transactionCount = (Long) result[2];

            CategoryResponseDTO dto = convertToDTO(category);
            dto.setTotalSpent(totalSpent != null ? totalSpent : 0.0);
            dto.setTransactionCount(transactionCount != null ? transactionCount : 0L);
            categories.add(dto);
        }

        return categories.stream()
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO getCategoryById(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        CategoryResponseDTO dto = convertToDTO(category);

        Double totalSpent = categoryRepository.getTotalSpentByCategory(user, id);
        Long transactionCount = categoryRepository.getTransactionCountByCategory(user, id);

        dto.setTotalSpent(totalSpent != null ? totalSpent : 0.0);
        dto.setTransactionCount(transactionCount != null ? transactionCount : 0L);

        return dto;
    }

    public CategoryResponseDTO getCategoryWithDetails(String email, Long id) {
        return getCategoryById(email, id);
    }

    @Transactional
    public CategoryResponseDTO createCategory(String email, CategoryRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if category with same name already exists
        if (categoryRepository.existsByUserAndName(user, dto.getName())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists");
        }

        Category category = new Category();
        category.setUser(user);
        category.setName(dto.getName());
        category.setIcon(dto.getIcon() != null ? dto.getIcon() : "category");
        category.setColor(dto.getColor() != null ? dto.getColor() : "#10B981");

        Category savedCategory = categoryRepository.save(category);

        CategoryResponseDTO responseDTO = convertToDTO(savedCategory);
        responseDTO.setTotalSpent(0.0);
        responseDTO.setTransactionCount(0L);

        return responseDTO;
    }

    @Transactional
    public CategoryResponseDTO updateCategory(String email, Long id, CategoryRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check if new name conflicts with existing category
        if (dto.getName() != null && !dto.getName().equals(category.getName())) {
            if (categoryRepository.existsByUserAndName(user, dto.getName())) {
                throw new RuntimeException("Category with name '" + dto.getName() + "' already exists");
            }
            category.setName(dto.getName());
        }

        if (dto.getIcon() != null) {
            category.setIcon(dto.getIcon());
        }

        if (dto.getColor() != null) {
            category.setColor(dto.getColor());
        }

        Category updatedCategory = categoryRepository.save(category);

        CategoryResponseDTO responseDTO = convertToDTO(updatedCategory);

        Double totalSpent = categoryRepository.getTotalSpentByCategory(user, id);
        Long transactionCount = categoryRepository.getTransactionCountByCategory(user, id);

        responseDTO.setTotalSpent(totalSpent != null ? totalSpent : 0.0);
        responseDTO.setTransactionCount(transactionCount != null ? transactionCount : 0L);

        return responseDTO;
    }

    @Transactional
    public void deleteCategory(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryRepository.delete(category);
    }

    public CategorySummaryDTO getCategorySummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CategorySummaryDTO summary = new CategorySummaryDTO();

        List<CategoryResponseDTO> categories = getUserCategories(email);
        summary.setCategories(categories);
        summary.setTotalCategories((long) categories.size());

        Double totalSpent = categoryRepository.getTotalSpending(user);
        summary.setTotalSpent(totalSpent != null ? totalSpent : 0.0);

        summary.setAveragePerCategory(categories.size() > 0 ?
                summary.getTotalSpent() / categories.size() : 0.0);

        if (!categories.isEmpty()) {
            CategoryResponseDTO topCategory = categories.stream()
                    .max(Comparator.comparing(CategoryResponseDTO::getTotalSpent))
                    .orElse(null);
            if (topCategory != null) {
                summary.setTopCategory(topCategory.getName());
            }
        }

        Map<String, Double> spendingByCategory = new HashMap<>();
        for (CategoryResponseDTO cat : categories) {
            spendingByCategory.put(cat.getName(), cat.getTotalSpent());
        }
        summary.setSpendingByCategory(spendingByCategory);

        return summary;
    }

    private CategoryResponseDTO convertToDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setIcon(category.getIcon());
        dto.setColor(category.getColor());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}