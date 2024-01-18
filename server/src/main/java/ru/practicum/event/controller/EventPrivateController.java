package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPrivateController {

    private final EventService service;

    @GetMapping
    public List<EventFullDto> getInitiatorEvents(@PathVariable long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        log.debug("Получен запрос Get /users/{userId}/events?from={}&size={}", from, size);
        return service.getInitiatorEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addNewEvent(@PathVariable long userId,
                                    @RequestBody @Valid NewEventDto dto) {
        log.debug("Получен запрос Post /users/{userId}/events");
        return service.addEvent(userId, dto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getInitiatorEventById(@PathVariable long userId,
                                     @PathVariable long eventId) {
        log.debug("Получен запрос Get /users/{userId}/events/{eventId}");
        return service.getCurrentInitiatorEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByInitiator(@PathVariable long userId,
                                    @PathVariable long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest dto) {
        log.debug("Получен запрос Patch /users/{userId}/events/{eventId}");
        return service.updateEventByInitiator(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getUserParticipationRequest(@PathVariable long userId,
                                                                     @PathVariable long eventId) {
        log.debug("Получен запрос Get /users/{userId}/events/{eventId}/requests");
        return service.getEventsParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusOfRequests(@PathVariable long userId,
                                                                 @PathVariable long eventId,
                                                                 @RequestBody
                                                                @Valid EventRequestStatusUpdateRequest requests) {
        log.debug("Получен запрос Patch /users/{userId}/events/{eventId}/requests");
        return service.confirmEventsParticipationRequests(userId, eventId, requests);
    }
}
