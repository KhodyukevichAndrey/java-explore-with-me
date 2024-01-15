package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@Slf4j
@RequiredArgsConstructor
public class CompilationAdminController {

    private final CompilationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody NewCompilationDto dto) {
        log.debug("Получен запрос Post /admin/compilations");
        return service.addCompilation(dto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compId) {
        log.debug("Получен запрос Delete /admin/compilations/{compId}");
        service.deleteCompilationById(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable long compId, @RequestBody UpdateCompilationRequest request) {
        log.debug("Получен запрос Patch /admin/compilations/{compId}");
        return service.updateCompilation(compId, request);
    }
}
