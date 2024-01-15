package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService service;
    private final StatsClient client;

    @GetMapping
    public List<EventShortDto> getEventsByFilter(@RequestParam(required = false) String text,
                                                 @RequestParam(required = false) Integer[] categories,
                                                 @RequestParam(required = false) Boolean paid,
                                                 @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                 @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                 @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                 @RequestParam(required = false) String sort,
                                                 @RequestParam(defaultValue = "0") @Min(0) @Max(25) int from,
                                                 @RequestParam(defaultValue = "10") @Min(0) @Max(25) int size,
                                                 HttpServletRequest request) {
        log.debug("Получен запрос Get /events");
        client.postEndpointHit(new EndpointHitDto(
                "ewm-main-service",
                "/events",
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));
        return service.getEventForNotRegistrationUserByFiltering(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable long id, HttpServletRequest request) {
        log.debug("Получен запрос Get /events/{id}");
        client.postEndpointHit(new EndpointHitDto(
                "ewm-main-service",
                "/events/" + id,
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));
        return service.getEventForNotRegistrationUserById(id);
    }
}