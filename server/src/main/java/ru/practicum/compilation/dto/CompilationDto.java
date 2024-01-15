package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {

    private List<EventShortDto> events;
    private long id;
    private boolean pinned;
    private String title;
}
