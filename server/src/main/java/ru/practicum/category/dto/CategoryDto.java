package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class CategoryDto {
    private long id;
    @Size(min = 1, max = 50)
    private String name;
}
