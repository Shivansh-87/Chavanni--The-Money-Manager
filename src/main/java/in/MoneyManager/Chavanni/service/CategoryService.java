package in.MoneyManager.Chavanni.service;

import in.MoneyManager.Chavanni.dto.CategoryDTO;
import in.MoneyManager.Chavanni.entity.CategoryEntity;
import in.MoneyManager.Chavanni.entity.ProfileEntity;
import in.MoneyManager.Chavanni.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
//import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    public CategoryDTO saveCategory(CategoryDTO categoryDTO){
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())) {
            throw new RuntimeException( "Category with the same name already exists");
        }
        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        newCategory= categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return entities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingcategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or accessible"));
        existingcategory.setName(dto.getName());
        existingcategory.setType(dto.getType());
        existingcategory.setIcon(dto.getIcon());
        existingcategory = categoryRepository.save(existingcategory);
        return toDTO(existingcategory);

    }

    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile){
        return  CategoryEntity.builder()
                .name(categoryDTO.getName())
                .type(categoryDTO.getType())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .build();
    }
    private CategoryDTO toDTO(CategoryEntity entity){
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .profileId(entity.getProfile() !=null ? entity.getProfile().getId() : null)
                .build();
    }
}
