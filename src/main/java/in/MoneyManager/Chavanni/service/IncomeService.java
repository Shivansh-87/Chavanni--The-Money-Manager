package in.MoneyManager.Chavanni.service;

import in.MoneyManager.Chavanni.dto.IncomeDTO;
import in.MoneyManager.Chavanni.entity.CategoryEntity;
import in.MoneyManager.Chavanni.entity.IncomeEntity;
import in.MoneyManager.Chavanni.entity.ProfileEntity;
import in.MoneyManager.Chavanni.repository.CategoryRepository;
import in.MoneyManager.Chavanni.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public IncomeDTO saveIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        CategoryEntity category = categoryRepository
                .findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));

        IncomeEntity entity = IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .category(category)
                .profile(profile)
                .build();

        entity = incomeRepository.save(entity);
        return toDTO(entity);
    }

    // Returns all incomes for current user filtered by date range
    public List<IncomeDTO> getIncomesByDateRange(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return incomeRepository
                .findByProfileIdAndDateBetween(profile.getId(), startDate, endDate)
                .stream().map(this::toDTO).toList();
    }

    // Returns all incomes filtered by date range + keyword search, with sorting
    public List<IncomeDTO> searchIncomes(LocalDate startDate, LocalDate endDate,
                                         String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return incomeRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate, endDate, keyword, sort)
                .stream().map(this::toDTO).toList();
    }

    // Returns latest 5 incomes
    public List<IncomeDTO> getTop5RecentIncomes() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return incomeRepository
                .findTop5ByProfileIdOrderByDateDesc(profile.getId())
                .stream().map(this::toDTO).toList();
    }

    // Returns total income amount
    public BigDecimal getTotalIncomes() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    public IncomeDTO updateIncome(Long incomeId, IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();

        IncomeEntity existing = incomeRepository
                .findByIdAndProfileId(incomeId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Income not found or not accessible"));

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

        existing = incomeRepository.save(existing);
        return toDTO(existing);
    }

    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity existing = incomeRepository
                .findByIdAndProfileId(incomeId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Income not found or not accessible"));
        incomeRepository.delete(existing);
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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