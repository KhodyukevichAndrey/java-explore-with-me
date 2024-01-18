package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @Min(-90)
    @Max(90)
    @NotNull
    private Float lat;
    @Min(-180)
    @Max(180)
    @NotNull
    private Float lon;
}
