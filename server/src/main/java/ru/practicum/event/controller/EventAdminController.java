package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Slf4j
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService service;

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) Integer[] users,
                                        @RequestParam(required = false) String[] states,
                                        @RequestParam(required = false) Integer[] categories,
                                        @RequestParam
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") @Min(0) @Max(25) int from,
                                        @RequestParam(defaultValue = "10") @Min(0) @Max(25) int size) {
        log.debug("Получен запрос Get /admin/events");
        return service.getEventByAdminFiltering(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventStatus(@PathVariable long eventId,
                                          @RequestBody @Valid UpdateEventAdminRequest dto) {
        log.debug("Получен запрос Patch /admin/events/{eventId}");
        return service.updateEventStatusByAdmin(eventId, dto);
    }

}
