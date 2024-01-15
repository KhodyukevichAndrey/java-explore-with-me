package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserShortDto {
    private long id;
    private String name;
}
