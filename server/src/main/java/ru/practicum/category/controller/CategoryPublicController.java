package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("categories")
@Slf4j
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam (defaultValue = "0") int from,
                                           @RequestParam (defaultValue = "10") int size) {
        log.debug("Получен запрос Get /categories");
        return service.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable long catId) {
        log.debug("Получен запрос Get /categories");
        return service.getCategory(catId);
    }
}
