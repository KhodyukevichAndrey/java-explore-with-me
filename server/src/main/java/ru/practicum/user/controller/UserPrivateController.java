package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UpdateUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserPrivateController {

    private final UserService service;

    @PatchMapping("/{userId}")
    public UserDto updateUserByUser(@PathVariable long userId, @RequestBody @Valid UpdateUserDto updateUserDto) {
        log.debug("Получен запрос Patch /users/{userId}");
        return service.updateUserByUser(userId, updateUserDto);
    }
}
