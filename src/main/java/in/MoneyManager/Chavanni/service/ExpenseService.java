package in.MoneyManager.Chavanni.service;

import in.MoneyManager.Chavanni.dto.ExpenseDTO;
import in.MoneyManager.Chavanni.entity.CategoryEntity;
import in.MoneyManager.Chavanni.entity.ExpenseEntity;
import in.MoneyManager.Chavanni.entity.ProfileEntity;
import in.MoneyManager.Chavanni.repository.CategoryRepository;
import in.MoneyManager.Chavanni.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public ExpenseDTO saveExpense(ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        CategoryEntity category = categoryRepository
                .findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));

        ExpenseEntity entity = ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .category(category)
                .profile(profile)
                .build();

        entity = expenseRepository.save(entity);
        return toDTO(entity);
    }

    // Returns all expenses for current user filtered by date range
    public List<ExpenseDTO> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return expenseRepository
                .findByProfileIdAndDateBetween(profile.getId(), startDate, endDate)
                .stream().map(this::toDTO).toList();
    }

    // Returns all expenses filtered by date range + keyword search, with sorting
    public List<ExpenseDTO> searchExpenses(LocalDate startDate, LocalDate endDate,
                                           String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return expenseRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate, endDate, keyword, sort)
                .stream().map(this::toDTO).toList();
    }

    // Returns latest 5 expenses
    public List<ExpenseDTO> getTop5RecentExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return expenseRepository
                .findTop5ByProfileIdOrderByDateDesc(profile.getId())
                .stream().map(this::toDTO).toList();
    }

    // Returns total expense amount
    public BigDecimal getTotalExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    public ExpenseDTO updateExpense(Long expenseId, ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        ExpenseEntity existing = expenseRepository
                .findByIdAndProfileId(expenseId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Expense not found or not accessible"));

        if (dto.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                    .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));
            existing.setCategory(category);
        }

        existing.setName(dto.getName());
        existing.setIcon(dto.getIcon());
        existing.setAmount(dto.getAmount());
        if (dto.getDate() != null) existing.setDate(dto.getDate());

        existing = expenseRepository.save(existing);
        return toDTO(existing);
    }

    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity existing = expenseRepository
                .findByIdAndProfileId(expenseId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Expense not found or not accessible"));
        expenseRepository.delete(existing);
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}