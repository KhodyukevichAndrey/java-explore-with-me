package ru.practicum.compilation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.storage.CompilationStorage;
import ru.practicum.constants.sort.SortConstants;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.request.status.Status;
import ru.practicum.request.storage.RequestStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static ru.practicum.constants.error.ErrorConstants.ID_START_FROM;
import static ru.practicum.constants.error.ErrorConstants.WRONG_COMPILATION_ID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationStorage compilationStorage;
    private final EventStorage eventStorage;
    private final RequestStorage requestStorage;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.makeCompilation(dto);
        Map<Long, Long> confirmedRequestByEventId = new HashMap<>();
        Map<Long, Long> viewsByEventId = new HashMap<>();

        if (dto.getEvents() != null) {
            addEventsToCompilation(compilation, new HashSet<>(dto.getEvents()));
            confirmedRequestByEventId = getConfirmedRequests(compilation.getEvents());
            viewsByEventId = getViews(compilation.getEvents());
        } else {
            compilation.setEvents(Collections.emptySet());
        }

        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }

        return CompilationMapper.makeDto(compilationStorage.save(compilation), makeEventShort(compilation.getEvents(),
                confirmedRequestByEventId, viewsByEventId));
    }

    @Override
    @Transactional
    public void deleteCompilationById(long compilationId) {
        getCompilation(compilationId);

        compilationStorage.deleteById(compilationId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest dto) {
        Compilation compilation = getCompilation(compilationId);

        if (dto.getPinned() != null) {
            compilation.setPinned(false);
        }
        if (dto.getEvents() == null || dto.getEvents().isEmpty()) {
            compilation.setEvents(Collections.emptySet());
        } else {
            addEventsToCompilation(compilation, dto.getEvents());
        }
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }

        compilation = compilationStorage.save(compilation);

        Map<Long, Long> confirmedRequestByEventId = getConfirmedRequests(compilation.getEvents());
        Map<Long, Long> viewsByEventId = getViews(compilation.getEvents());

        return CompilationMapper.makeDto(compilation, makeEventShort(compilation.getEvents(),
                confirmedRequestByEventId, viewsByEventId));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        List<CompilationDto> compilationDto = new ArrayList<>();

        if (pinned == null) {
            compilations = compilationStorage
                    .findAll(PageRequest.of(from / size, size, SortConstants.SORT_BY_ID_ASC)).getContent();
        } else {
            compilations = compilationStorage.findByPinned(pinned,
                    PageRequest.of(from / size, size, SortConstants.SORT_BY_ID_ASC));
        }

        for (Compilation compilation : compilations) {
            Map<Long, Long> confirmedRequestByEventId = getConfirmedRequests(compilation.getEvents());
            Map<Long, Long> viewsByEventId = getViews(compilation.getEvents());
            compilationDto.add(CompilationMapper.makeDto(compilation, makeEventShort(compilation.getEvents(),
                    confirmedRequestByEventId, viewsByEventId)));
        }

        return compilationDto;
    }

    @Override
    public CompilationDto getCompilationById(long compId) {
        Compilation compilation = getCompilation(compId);

        Map<Long, Long> confirmedRequestByEventId = getConfirmedRequests(compilation.getEvents());
        Map<Long, Long> viewsByEventId = getViews(compilation.getEvents());

        return CompilationMapper.makeDto(compilation, makeEventShort(compilation.getEvents(),
                confirmedRequestByEventId, viewsByEventId));
    }

    private Compilation getCompilation(long compilationId) {
        return compilationStorage.findById(compilationId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_COMPILATION_ID));
    }

    private Compilation addEventsToCompilation(Compilation compilation, Set<Long> eventsId) {
        if (!eventsId.isEmpty()) {
            compilation.setEvents(eventStorage.findEventByIdIn(eventsId));
        }
        return compilation;
    }

    private Map<Long, Long> getConfirmedRequests(Set<Event> events) {
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .collect(toList());

        return requestStorage.findParticipationRequestByEventIdInAndStatus(eventsIds, Status.CONFIRMED)
                .stream()
                .collect(groupingBy(pr -> pr.getEvent().getId(), Collectors.counting()));
    }

    private Map<Long, Long> getViews(Set<Event> events) {
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

    private Set<EventShortDto> makeEventShort(Set<Event> events, Map<Long, Long> confirmed, Map<Long, Long> views) {
        if (events.isEmpty()) {
            return Collections.emptySet();
        }
        return events.stream()
                .map(event ->
                        EventMapper.makeEventShortDto(event,
                                confirmed.getOrDefault(event.getId(), 0L),
                                views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toSet());
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
