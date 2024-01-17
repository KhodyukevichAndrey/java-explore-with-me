package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.client.StatsClient;
import ru.practicum.constants.error.ErrorConstants;
import ru.practicum.constants.sort.SortConstants;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.category.storage.CategoryStorage;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.state.StateAction;
import ru.practicum.exception.ConflictException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.status.Status;
import ru.practicum.request.storage.RequestStorage;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static ru.practicum.constants.error.ErrorConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final CategoryStorage categoryStorage;
    private final RequestStorage requestStorage;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    // For EventPrivateController
    @Override
    public List<EventFullDto> getInitiatorEvents(long userId, int from, int size) {
        getUser(userId);
        List<Event> events = eventStorage.findEventByInitiatorId(userId,
                PageRequest.of(from / size, size, SortConstants.SORT_BY_ID_ASC));

        Map<Long, Long> confirmed = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);

        return makeEventFull(events, confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, NewEventDto newEventDto) {
        Category category = getCat(newEventDto.getCategory());
        User initiator = getUser(userId);
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(BAD_START_TIME);
        }
        Event event = EventMapper.makeEvent(newEventDto, initiator, category);

        return EventMapper.makeEventFullDto(eventStorage.save(event), 0, 0);
    }

    @Override
    public EventFullDto getCurrentInitiatorEvent(long userId, long eventId) {
        getUser(userId);
        Event event = getEvent(eventId);
        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

        if (event.getInitiator().getId() == userId) {
            return makeEventFull(List.of(event), confirmed, views).get(0);
        } else {
            throw new ConflictException(ErrorConstants.WRONG_COMBINATION_OF_INIT_AND_EVENT);
        }
    }

    @Override
    @Transactional
    public EventFullDto updateEventByInitiator(long userId, long eventId, UpdateEventUserRequest dto) {
        getUser(userId);
        Event event = getEvent(eventId);
        StateAction stateAction = dto.getStateAction();

        if (dto.getCategory() != null) {
            event.setCategory(getCat(dto.getCategory()));
        }

        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

        completeFieldsByUserUpdate(dto, event);

        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(EVENT_NOT_AVAILABLE_STATE);
        }

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(BAD_START_TIME);
        }

        if (stateAction != null) {
            if (stateAction.equals(StateAction.SEND_TO_REVIEW)) {
                event.setEventState(EventState.PENDING);
            } else if (stateAction.equals(StateAction.CANCEL_REVIEW)) {
                event.setEventState(EventState.CANCELED);
            }
        }

        if (event.getInitiator().getId() != userId) {
            throw new ConflictException(ErrorConstants.WRONG_COMBINATION_OF_INIT_AND_EVENT);
        }

        return makeEventFull(List.of(eventStorage.save(event)), confirmed, views).get(0);
    }

    @Override
    public List<ParticipationRequestDto> getEventsParticipationRequests(long userId, long eventId) {
        getUser(userId);
        getEvent(eventId);

        List<ParticipationRequest> requests = requestStorage.findParticipationRequestByEvent(eventId);
        return requests.stream()
                .map(RequestMapper::makeRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult confirmEventsParticipationRequests(long userId, long eventId,
                                                                             EventRequestStatusUpdateRequest requests) {
        getUser(userId);
        Event event = getEvent(eventId);
        long participationLimit = event.getParticipantLimit();
        String status = requests.getStatus();
        Long[] ids = requests.getRequestIds();
        long participationCount = requestStorage.getCountOfParticipation(eventId, Status.CONFIRMED);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return getRequestWithoutLimitAndModeration(ids);
        }

        if (participationLimit != 0 && participationCount == participationLimit) {
            throw new ConflictException(ErrorConstants.OUT_OF_LIMIT);
        }

        if (event.getInitiator().getId() != userId) {
            throw new ConflictException(ErrorConstants.WRONG_COMBINATION_OF_INIT_AND_EVENT);
        }

        if (status.equals(Status.CONFIRMED.toString())) {
            return confirmRequests(ids, participationLimit, participationCount);
        } else if (status.equals(Status.REJECTED.toString())) {
            return rejectRequests(ids);
        } else {
            throw new ConflictException(WRONG_STATUS);
        }
    }

    // For EventAdminController

    @Override
    public List<EventFullDto> getEventByAdminFiltering(List<Long> users, List<EventState> states, List<Long> categories,
                                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                       int from, int size) {

        List<Event> events = eventStorage.findEventByAdminParameters(users, states, categories, rangeStart,
                rangeEnd, PageRequest.of(from / size, size, SortConstants.SORT_BY_ID_ASC));
        Map<Long, Long> confirmed = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);

        return makeEventFull(events, confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEventStatusByAdmin(long eventId, UpdateEventAdminRequest dto) {
        Event event = getEvent(eventId);
        LocalDateTime eventDate = event.getEventDate();
        StateAction stateAction = dto.getStateAction();
        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

        completeFieldsByAdminUpdate(dto, event);

        if (!eventDate.isAfter(LocalDateTime.now().plusHours(1L))) {
            throw new ConflictException(ErrorConstants.EVENT_START_TIME);
        }
        if (!event.getEventState().equals(EventState.PENDING)) {
            throw new ConflictException(EVENT_NOT_AVAILABLE_STATE);
        }

        if (stateAction != null) {
            if (stateAction.equals(StateAction.PUBLISH_EVENT)) {
                event.setEventState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                eventStorage.save(event);
            } else if (stateAction.equals(StateAction.REJECT_EVENT)) {
                event.setEventState(EventState.CANCELED);
                eventStorage.save(event);
            }
        }

        return makeEventFull(List.of(event), confirmed, views).get(0);
    }

    // For EventPublicController

    @Override
    public List<EventShortDto> getEventForNotRegistrationUserByFiltering(String text, List<Long> categories, Boolean isPaid,
                                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                         Boolean onlyAvailable, String sort, int from, int size) {
        Sort currentSort;
        if (rangeStart != null && rangeEnd != null && (rangeStart.isAfter(rangeEnd))) {
            throw new ConflictException("WRONG CONDITION");
        }

        if (sort != null && sort.equalsIgnoreCase("EVENT_DATE")) {
            currentSort = SortConstants.SORT_BY_EVENT_DATE_ASC;
        } else if (sort != null && sort.equalsIgnoreCase("VIEWS")) {
            currentSort = SortConstants.SORT_BY_VIEWS_DESC;
        } else {
            currentSort = SortConstants.SORT_BY_ID_ASC;
        }

        List<Event> events = eventStorage.findEventByNotRegistrationUser(text, categories, isPaid, rangeStart, rangeEnd,
                onlyAvailable, PageRequest.of(from / size, size, currentSort));

        Map<Long, Long> confirmedRequest = getConfirmedRequests(events);
        Map<Long, Long> views = getViews(events);

        return makeEventShort(events, confirmedRequest, views);
    }

    @Override
    public EventFullDto getEventForNotRegistrationUserById(long eventId) {
        Event event = getEvent(eventId);
        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

        if (event.getEventState().equals(EventState.PUBLISHED)) {
            return makeEventFull(List.of(event), confirmed, views).get(0);
        } else {
            throw new EntityNotFoundException(WRONG_EVENT_ID);
        }
    }

    private User getUser(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorConstants.WRONG_USER_ID));
    }

    private Event getEvent(long eventId) {
        return eventStorage.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorConstants.WRONG_EVENT_ID));
    }

    private Category getCat(long catId) {
        return categoryStorage.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorConstants.WRONG_CAT_ID));
    }

    private EventRequestStatusUpdateResult confirmRequests(Long[] ids, long limit, long participationCount) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequest> requests = new ArrayList<>();

        Map<Long, ParticipationRequest> requestsById = requestStorage.findParticipationRequestByIdIn(ids)
                .stream()
                .collect(Collectors.toMap(ParticipationRequest::getId, Function.identity()));

        if (limit == 0) {
            for (Long id : ids) {
                ParticipationRequest request = requestsById.get(id);
                if (!request.getStatus().equals(Status.PENDING)) {
                    throw new ConflictException(ErrorConstants.WRONG_PARTICIPATION_STATUS);
                } else {
                    request.setStatus(Status.CONFIRMED);
                    result.getConfirmedRequests().add(RequestMapper.makeRequestDto(requestStorage.save(request)));
                }
            }
        } else {
            for (Long id : ids) {
                ParticipationRequest request = requestsById.get(id);

                if (!request.getStatus().equals(Status.PENDING)) {
                    throw new ConflictException(ErrorConstants.WRONG_PARTICIPATION_STATUS);
                } else {
                    if (participationCount < limit) {
                        request.setStatus(Status.CONFIRMED);
                        requests.add(request);
                        result.getConfirmedRequests().add(RequestMapper.makeRequestDto(request));
                    } else {
                        request.setStatus(Status.REJECTED);
                        requests.add(request);
                        result.getRejectedRequests().add(RequestMapper.makeRequestDto(request));
                    }
                }
            }
            requestStorage.saveAll(requests);
        }
        return result;
    }

    private EventRequestStatusUpdateResult rejectRequests(Long[] ids) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequest> requests = new ArrayList<>();

        Map<Long, ParticipationRequest> requestsById = requestStorage.findParticipationRequestByIdIn(ids)
                .stream()
                .collect(toMap(ParticipationRequest::getId, Function.identity()));

        for (Long id : ids) {
            ParticipationRequest request = requestsById.get(id);

            if (!request.getStatus().equals(Status.PENDING)) {
                throw new ConflictException(ErrorConstants.WRONG_PARTICIPATION_STATUS);
            } else {
                request.setStatus(Status.REJECTED);
                requests.add(request);
                result.getRejectedRequests().add(RequestMapper.makeRequestDto(request));
            }
        }
        requestStorage.saveAll(requests);
        return result;
    }

    private EventRequestStatusUpdateResult getRequestWithoutLimitAndModeration(Long[] ids) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        result.getConfirmedRequests().addAll(requestStorage.findParticipationRequestByIdIn(ids).stream()
                .map(RequestMapper::makeRequestDto)
                .collect(toList()));

        return result;
    }

    private Event completeFieldsByAdminUpdate(UpdateEventAdminRequest dto, Event event) {
        if (dto.getAnnotation() != null && !dto.getAnnotation().isBlank()) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(getCat(dto.getCategory()));
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }
        if (dto.getPaid() != null) {
            event.setIsPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        return event;
    }

    private Event completeFieldsByUserUpdate(UpdateEventUserRequest dto, Event event) {
        StateAction stateAction = dto.getStateAction();

        if (dto.getAnnotation() != null && !dto.getAnnotation().isBlank()) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(getCat(dto.getCategory()));
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) {
            event.setLocation(dto.getLocation());
        }
        if (dto.getPaid() != null) {
            event.setIsPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (stateAction != null) {
            if (stateAction == StateAction.PUBLISH_EVENT) {
                event.setEventState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (stateAction == StateAction.SEND_TO_REVIEW) {
                event.setEventState(EventState.PENDING);
            } else if (stateAction == StateAction.CANCEL_REVIEW) {
                event.setEventState(EventState.CANCELED);
            }
        }
        return event;
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .collect(toList());

        return requestStorage.findParticipationRequestByEventIdInAndStatus(eventsIds, Status.CONFIRMED)
                .stream()
                .collect(groupingBy(pr -> pr.getEvent().getId(), Collectors.counting()));
    }

    private Map<Long, Long> getViews(List<Event> events) {
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
                .collect(toMap(endpoint -> Long.parseLong(endpoint.getUri().substring(9)),
                        EndpointHitStatsDto::getHits));
    }

    private List<EventFullDto> makeEventFull(List<Event> events, Map<Long, Long> confirmed, Map<Long, Long> views) {
        return events.stream()
                .map(event ->
                        EventMapper.makeEventFullDto(event,
                                confirmed.getOrDefault(event.getId(), 0L),
                                views.getOrDefault(event.getId(), 0L)))
                .collect(toList());
    }

    private List<EventShortDto> makeEventShort(List<Event> events, Map<Long, Long> confirmed, Map<Long, Long> views) {
        return events.stream()
                .map(event ->
                        EventMapper.makeEventShortDto(event,
                                confirmed.getOrDefault(event.getId(), 0L),
                                views.getOrDefault(event.getId(), 0L)))
                .collect(toList());
    }

    private List<EndpointHitStatsDto> getStatsDto(LocalDateTime rangeStart, LocalDateTime rangeEnd, String[] urisArray,
                                                  boolean unique) {
        ResponseEntity<Object> responseEntity = statsClient.getStats(rangeStart, rangeEnd, urisArray, unique);
        if (responseEntity.getBody() == null) {
            return Collections.emptyList();
        }
        List<EndpointHitStatsDto> statsDtos = objectMapper.convertValue(responseEntity.getBody(), new TypeReference<>() {
        });

        return objectMapper.convertValue(responseEntity.getBody(), new TypeReference<List<EndpointHitStatsDto>>() {
        });
    }
}
