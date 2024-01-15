package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/compilations")
@Slf4j
@RequiredArgsConstructor
public class CompilationPublicController {

    private final CompilationService service;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam Boolean pinned,
                                                @RequestParam(defaultValue = "0") @Min(0) @Max(25) int from,
                                                @RequestParam(defaultValue = "10") @Min(0) @Max(25) int size) {
        log.debug("Получен запрос Get /compilations");
        return service.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable long compId) {
        log.debug("Получен запрос Get /compilations/{compId}");
        return service.getCompilationById(compId);
    }
}
