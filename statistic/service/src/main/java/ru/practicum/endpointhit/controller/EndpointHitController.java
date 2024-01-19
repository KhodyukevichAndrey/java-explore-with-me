package ru.practicum.endpointhit.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.endpointhit.service.EndpointHitService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EndpointHitController {

    private final EndpointHitService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto addStatNode(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.debug("Получен запрос POST /hit");
        return service.addCallEndpointHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<EndpointHitStatsDto> getStats(@RequestParam
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                              @RequestParam
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                              @RequestParam(name = "uris", required = false) String[] uris,
                                              @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        log.debug("Получен запрос GET /stats?start={start}&end={end}&uris={uris}&unique={unique}");
        return service.getStats(start, end, uris, unique);
    }
}
