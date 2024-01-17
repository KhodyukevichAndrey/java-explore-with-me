package ru.practicum.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EndpointHitStatsDto {
    private String app;
    private String uri;
    private long hits;
}
