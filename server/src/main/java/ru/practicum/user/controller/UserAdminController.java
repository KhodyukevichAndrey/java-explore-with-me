package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserAdminController {

    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid NewUserRequest userRequest) {
        log.debug("Получен запрос POST /admin/users");
        return service.addUser(userRequest);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) Integer[] ids,
                                  @RequestParam(defaultValue = "0") @Min(0) @Max(50) int from,
                                  @RequestParam(defaultValue = "10") @Min(0) @Max(50) int size) {
        return service.getUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        service.deleteUser(userId);
    }
}
