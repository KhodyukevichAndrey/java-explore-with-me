package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.constants.error.ErrorConstants;
import ru.practicum.constants.sort.SortConstants;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.category.storage.CategoryStorage;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.state.StateAction;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotAvailableException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.status.Status;
import ru.practicum.request.storage.RequestStorage;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserStorage;
import ru.practicum.endpointhit.storage.EndpointHitStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static ru.practicum.constants.error.ErrorConstants.WRONG_STATUS;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final CategoryStorage categoryStorage;
    private final RequestStorage requestStorage;
    private final EndpointHitStorage endpointHitStorage;

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
    public EventFullDto updateEventByInitiator(long userId, long eventId, NewEventDto dto) {
        User user = getUser(userId);
        Category category = getCat(dto.getCategory());
        Event event = getEvent(eventId);
        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = getViews(List.of(event));

        if (event.getInitiator().getId() == userId) {
            Event eventForUpdate = EventMapper.makeEvent(dto, user, category);
            eventStorage.save(eventForUpdate);
            return makeEventFull(List.of(eventForUpdate), confirmed, views).get(0);
        } else {
            throw new ConflictException(ErrorConstants.WRONG_COMBINATION_OF_INIT_AND_EVENT);
        }
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

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
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
    public List<EventFullDto> getEventByAdminFiltering(Integer[] users, String[] states, Integer[] categories,
                                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                       int from, int size) {

        List<Event> events = eventStorage.findEventByAdminParameters(users, states, categories, rangeStart,
                rangeEnd, PageRequest.of(from / size, size, SortConstants.SORT_BY_EVENT_DATE_ASC));
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

        completeFields(dto, event);

        if (!eventDate.isAfter(LocalDateTime.now().plusHours(1L))) {
            throw new NotAvailableException(ErrorConstants.EVENT_START_TIME);
        }
        if (!event.getEventState().equals(EventState.PENDING)) {
            throw new NotAvailableException(ErrorConstants.EVENT_NOT_AVAILABLE_STATE);
        }
        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new NotAvailableException(ErrorConstants.EVENT_NOT_AVAILABLE_STATE);
        }

        Event eventForUpdate = EventMapper.makeEventForUpdate(dto, event.getInitiator(), event.getCategory(), eventId);
        if (stateAction.equals(StateAction.PUBLISH_EVENT)) {
            eventForUpdate.setEventState(EventState.PUBLISHED);
            eventStorage.save(eventForUpdate);
            return makeEventFull(List.of(eventForUpdate), confirmed, views).get(0);
        } else if (stateAction.equals(StateAction.REJECT_EVENT)) {
            eventForUpdate.setEventState(EventState.CANCELED);
            eventStorage.save(eventForUpdate);
            return makeEventFull(List.of(eventForUpdate), confirmed, views).get(0);
        } else {
            throw new NotAvailableException("Недопустимый статус события");
        }
    }

    // For EventPublicController

    @Override
    public List<EventShortDto> getEventForNotRegistrationUserByFiltering(String text, Integer[] categories, Boolean isPaid,
                                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                         Boolean onlyAvailable, String sort, int from, int size) {
        List<Event> events;
        if (sort != null && sort.equalsIgnoreCase("EVENT_DATE")) {
            events = eventStorage.findEventByNotRegistrationUser(text, categories, isPaid, rangeStart, rangeEnd,
                    onlyAvailable, PageRequest.of(from / size, size, SortConstants.SORT_BY_EVENT_DATE_ASC));
        } else if (sort != null && sort.equalsIgnoreCase("VIEWS")) {
            events = eventStorage.findEventByNotRegistrationUser(text, categories, isPaid, rangeStart, rangeEnd,
                    onlyAvailable, PageRequest.of(from / size, size, SortConstants.SORT_BY_VIEWS_DESC));
        } else {
            events = eventStorage.findEventByNotRegistrationUser(text, categories, isPaid, rangeStart, rangeEnd,
                    onlyAvailable, PageRequest.of(from / size, size, SortConstants.SORT_BY_ID_ASC));
        }
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
            return null;
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

    private UpdateEventAdminRequest completeFields(UpdateEventAdminRequest dto, Event event) {
        if (dto.getAnnotation() == null) {
            dto.setAnnotation(event.getAnnotation());
        }
        if (dto.getCategory() == null) {
            dto.setCategory(event.getCategory().getId());
        }
        if (dto.getDescription() == null) {
            dto.setDescription(event.getDescription());
        }
        if (dto.getEventDate() == null) {
            dto.setEventDate(event.getEventDate());
        }
        if (dto.getLocation() == null) {
            dto.setLocation(event.getLocation());
        }
        if (dto.getPaid() == null) {
            dto.setPaid(event.isPaid());
        }
        if (dto.getParticipantLimit() == null) {
            dto.setParticipantLimit(event.getParticipantLimit());
        }
        if (dto.getRequestModeration() == null) {
            dto.setRequestModeration(event.isRequestModeration());
        }
        if (dto.getTitle() == null) {
            dto.setTitle(event.getTitle());
        }
        return dto;
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

        List<EndpointHitStatsDto> stats = endpointHitStorage.findEndpointHitForUriInAndUnique(rangeStart.minusHours(1),
                LocalDateTime.now(), urisArray);

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
}
