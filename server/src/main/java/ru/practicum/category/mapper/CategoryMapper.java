package ru.practicum.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

@UtilityClass
public class CategoryMapper {

    public Category makeCat(NewCategoryDto categoryDto) {
        return new Category(
                0,
                categoryDto.getName()
        );
    }

    public CategoryDto makeCatDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }
}
