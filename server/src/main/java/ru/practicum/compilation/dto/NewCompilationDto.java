package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    @NotNull
    private List<Long> eventsId;
    private boolean pinned = false; // default
    @Size(min = 1, max = 50)
    @NotBlank
    private String title;
}
