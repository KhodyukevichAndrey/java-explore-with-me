package ru.practicum.service;


import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;

import java.util.List;

public interface EndpointHitService {

    EndpointHitDto addCallEndpointHit(EndpointHitDto dto);

    List<EndpointHitStatsDto> getStats(String start, String end, String[] uris, Boolean uniqueUris);
}
