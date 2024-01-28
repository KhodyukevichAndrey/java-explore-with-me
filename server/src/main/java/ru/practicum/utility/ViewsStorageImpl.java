package ru.practicum.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ru.practicum.constants.error.ErrorConstants.ID_START_FROM;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViewsStorageImpl implements ViewsStorage {

    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    public Map<Long, Long> getViews(Set<Event> events) {
        List<Event> publishedEvents = events.stream()
                .filter(event -> event.getEventState() == EventState.PUBLISHED)
                .collect(toList());

        if (publishedEvents.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = events.stream()
                .map(Event::getId)
                .map(id -> "/events/" + id)
                .collect(toList());
        String[] urisArray = uris.toArray(new String[]{});

        LocalDateTime rangeStart = publishedEvents.stream()
                .map(Event::getPublishedOn)
                .min(Comparator.naturalOrder()).get();

        List<EndpointHitStatsDto> stats = getStatsDto(rangeStart.minusHours(1), LocalDateTime.now(), urisArray,
                true);

        return stats.stream()
                .collect(toMap(endpoint -> Long.parseLong(endpoint.getUri().substring(ID_START_FROM)),
                        EndpointHitStatsDto::getHits));
    }

    private List<EndpointHitStatsDto> getStatsDto(LocalDateTime rangeStart, LocalDateTime rangeEnd, String[] urisArray,
                                                  boolean unique) {
        ResponseEntity<Object> responseEntity = statsClient.getStats(rangeStart, rangeEnd, urisArray, unique);

        if (responseEntity.getBody() == null) {
            return Collections.emptyList();
        }

        return objectMapper.convertValue(responseEntity.getBody(), new TypeReference<>() {
        });
    }
}
