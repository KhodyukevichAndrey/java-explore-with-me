package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto dto);

    void deleteCompilationById(long compilationId);

    CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest dto);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(long compId);
}
