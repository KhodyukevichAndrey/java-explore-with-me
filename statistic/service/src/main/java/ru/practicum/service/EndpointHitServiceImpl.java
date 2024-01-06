package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.exception.ValidateException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.storage.EndpointHitStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public List<EndpointHitStatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {

        if (end.isBefore(start)) {
            log.debug("Unacceptable value of start/end datetime");
            throw new ValidateException("Start must be before End");
        }

        if (uris == null) {
            if (unique != null && unique) {
                return storage.findAllEndpointHitForUnique(start, end);
            } else {
                return storage.findAllEndpointHitByDate(start, end);
            }
        } else {
            if (unique != null && unique) {
                return storage.findEndpointHitForUriInAndUnique(start, end, uris);
            } else {
                return storage.findEndpointHitForUriIn(start, end, uris);
            }
        }
    }
}
