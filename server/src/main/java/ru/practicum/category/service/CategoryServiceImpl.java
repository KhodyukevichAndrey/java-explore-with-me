package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.constants.error.ErrorConstants;
import ru.practicum.constants.sort.SortConstants;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.storage.CategoryStorage;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.constants.error.ErrorConstants.EVENT_CATEGORY_EXIST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryStorage storage;
    private final EventStorage eventStorage;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = storage.save(CategoryMapper.makeCat(newCategoryDto));

        return CategoryMapper.makeCatDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        getCat(catId);
        if (eventStorage.existsEventByCategoryId(catId)) {
            throw new ConflictException(EVENT_CATEGORY_EXIST);
        } else {
            storage.deleteById(catId);
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, NewCategoryDto newCategoryDto) {
        Category category = getCat(catId);

        category.setName(newCategoryDto.getName());

        return CategoryMapper.makeCatDto(storage.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        List<Category> categories = storage.findAll(PageRequest.of(from / size, size, SortConstants.SORT_BY_ID_ASC))
                .getContent();
        return categories.stream()
                .map(CategoryMapper::makeCatDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(long catId) {
        return CategoryMapper.makeCatDto(getCat(catId));
    }

    private Category getCat(long catId) {
        return storage.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorConstants.WRONG_CAT_ID));
    }
}
