package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @Size(min = 20, max = 2000)
    @NotBlank
    private String annotation;
    @NotNull
    private Long category;
    @Size(min = 20, max = 7000)
    @NotBlank
    private String description;
    @NotNull
    @FutureOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @Valid
    @NotNull
    private LocationDto location;
    @NotNull
    private Boolean paid = false; //default
    @NotNull
    @PositiveOrZero
    private Long participantLimit = 0L; //default
    @NotNull
    private Boolean requestModeration = true; //default
    @Size(min = 3, max = 120)
    @NotBlank
    private String title;

}
