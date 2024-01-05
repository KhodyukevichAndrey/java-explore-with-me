package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.storage.EndpointHitStorage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitStorage storage;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EndpointHitDto addCallEndpointHit(EndpointHitDto dto) {
        return EndpointHitMapper.makeEndpointHitDto(storage.save(EndpointHitMapper.makeEndpointHit(dto)));
    }

    @Override
    public List<EndpointHitStatsDto> getStats(String start, String end, String[] uris, Boolean unique) {
        LocalDateTime startTime = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), FORMATTER);

        if (endTime.isBefore(startTime)) {
            return Collections.emptyList();
        }

        if (uris == null) {
            if (unique != null && unique) {
                return storage.findAllEndpointHitForUnique(startTime, endTime);
            } else {
                return storage.findAllEndpointHitByDate(startTime, endTime);
            }
        } else {
            if (unique != null && unique) {
                return storage.findEndpointHitForUriInAndUnique(startTime, endTime, uris);
            } else {
                return storage.findEndpointHitForUriIn(startTime, endTime, uris);
            }
        }
    }
}
