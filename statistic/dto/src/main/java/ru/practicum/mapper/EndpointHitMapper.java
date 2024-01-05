package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;

@UtilityClass
public class EndpointHitMapper {

    public EndpointHit makeEndpointHit(EndpointHitDto dto) {
        return new EndpointHit(
                0,
                dto.getApp(),
                dto.getUri(),
                dto.getIp(),
                LocalDateTime.now()
        );
    }

    public EndpointHitDto makeEndpointHitDto(EndpointHit endpointHit) {
        return new EndpointHitDto(
                endpointHit.getApp(),
                endpointHit.getUri(),
                endpointHit.getIp()
        );
    }
}
