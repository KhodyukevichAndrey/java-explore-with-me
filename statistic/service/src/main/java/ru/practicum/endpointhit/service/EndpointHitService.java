package ru.practicum.endpointhit.service;


import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitService {

    EndpointHitDto addCallEndpointHit(EndpointHitDto dto);

    List<EndpointHitStatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean uniqueUris);
}
