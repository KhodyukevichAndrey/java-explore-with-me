package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CompilationMapper {

    public Compilation makeCompilation(NewCompilationDto dto) {
        return new Compilation(
                0,
                new ArrayList<>(),
                dto.isPinned(),
                dto.getTitle()
        );
    }

    public Compilation makeCompilationForUpdate(UpdateCompilationRequest request, long compilationId) {
        return new Compilation(
                compilationId,
                new ArrayList<>(),
                request.getPinned(),
                request.getTitle()
        );
    }

    public CompilationDto makeDto(Compilation compilation, List<EventShortDto> eventShortDto) {
        return new CompilationDto(
                eventShortDto,
                compilation.getId(),
                compilation.isPinned(),
                compilation.getTitle()
        );
    }
}
