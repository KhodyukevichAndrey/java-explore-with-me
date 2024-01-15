package ru.practicum.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {
    @Min(-90)
    @Max(90)
    private float lat;
    @Min(-180)
    @Max(180)
    private float lon;
}
