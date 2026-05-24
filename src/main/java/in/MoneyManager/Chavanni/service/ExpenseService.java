package in.MoneyManager.Chavanni.service;

import in.MoneyManager.Chavanni.dto.ExpenseDTO;
import in.MoneyManager.Chavanni.dto.FilterDTO;
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

    public List<ExpenseDTO> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return expenseRepository
                .findByProfileIdAndDateBetween(profile.getId(), startDate, endDate)
                .stream().map(this::toDTO).toList();
    }

    public List<ExpenseDTO> filterExpenses(FilterDTO filterDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Sort sort = Sort.by(
                Sort.Direction.fromString(
                        filterDTO.getSortOrder() != null ? filterDTO.getSortOrder() : "DESC"),
                filterDTO.getSortField() != null ? filterDTO.getSortField() : "date"
        );
        List<ExpenseEntity> list = expenseRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(),
                        filterDTO.getStartDate(),
                        filterDTO.getEndDate(),
                        filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "",
                        sort);
        return list.stream().map(this::toDTO).toList();
    }

    public List<ExpenseDTO> getTop5RecentExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return expenseRepository
                .findTop5ByProfileIdOrderByDateDesc(profile.getId())
                .stream().map(this::toDTO).toList();
    }

    // For controllers — gets profileId from security context
    public List<ExpenseDTO> getExpensesForUserOnDate(LocalDate date) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository
                .findByProfileIdAndDate(profile.getId(), date);
        return list.stream().map(this::toDTO).toList();
    }

    // For internal/scheduled jobs — profileId passed directly
    public List<ExpenseDTO> findByProfileIdAndDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> list = expenseRepository
                .findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDTO).toList();
    }

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