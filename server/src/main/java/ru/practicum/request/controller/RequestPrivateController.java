package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestPrivateController {

    private final RequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable long userId, @RequestParam long eventId) {
        log.debug("Получен запрос Post /users/{userId}/requests");
        return service.addRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getUsersRequests(@PathVariable long userId) {
        log.debug("Получен запрос Get /users/{userId}/requests");
        return service.getUsersRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipation(@PathVariable long userId, @PathVariable long requestId) {
        log.debug("Получен запрос Get /users/{userId}/requests/{requestId}/cancel");
        return service.cancelUserRequest(userId, requestId);
    }
}
