package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private long id;
    private String email;
    private String name;
    private Boolean isPublic;
}
