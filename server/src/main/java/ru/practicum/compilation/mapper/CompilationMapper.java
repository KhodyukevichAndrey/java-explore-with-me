package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class CompilationMapper {

    public Compilation makeCompilation(NewCompilationDto dto) {
        return new Compilation(
                0,
                new HashSet<>(),
                dto.getPinned(),
                dto.getTitle()
        );
    }

    public CompilationDto makeDto(Compilation compilation, Set<EventShortDto> eventShortDto) {
        return new CompilationDto(
                compilation.getId(),
                eventShortDto,
                compilation.getPinned(),
                compilation.getTitle()
        );
    }
}
