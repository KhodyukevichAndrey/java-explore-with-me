package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.service.EndpointHitService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class EndpointHitController {

    private final EndpointHitService service;

    @PostMapping("/hit")
    public ResponseEntity<String> addStatNode(@Validated @RequestBody EndpointHitDto endpointHitDto) {
        service.addCallEndpointHit(endpointHitDto);
        return new ResponseEntity<>("Данные успешно добавлены", HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public List<EndpointHitStatsDto> getStats(@RequestParam(name = "start") String start,
                                              @RequestParam(name = "end") String end,
                                              @RequestParam(name = "uris", required = false) String[] uris,
                                              @RequestParam(name = "unique", required = false) Boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}
