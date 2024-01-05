package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EndpointHitStatsDto {
    private String app;
    private String uri;
    private long hits;
}
