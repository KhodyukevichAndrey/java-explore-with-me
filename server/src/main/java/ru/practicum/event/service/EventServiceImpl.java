package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.constants.error.ErrorConstants;
import ru.practicum.constants.sort.SortConstants;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.category.storage.CategoryStorage;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotAvailableException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.EventConfirmedParticipation;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.status.Status;
import ru.practicum.request.storage.RequestStorage;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserStorage;
import ru.practicum.utility.Utils;

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
    private final Utils utils;

    // For EventPrivateController
    @Override
    public List<EventFullDto> getInitiatorEvents(long userId, int from, int size) {
        getUser(userId);
        List<Event> events = eventStorage.findEventByInitiatorId(userId,
                PageRequest.of(from / size, size, SortConstants.SORT_EVENT_BY_ID_DESC));

        Map<Long, Long> confirmed = getConfirmedRequests(events);
        Map<Long, Long> views = utils.getViews(new HashSet<>(events));

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
        Event event = eventStorage.save(EventMapper.makeEvent(newEventDto, initiator, category));

        return EventMapper.makeEventFullDto(eventStorage.save(event), 0, 0);
    }

    @Override
    public EventFullDto getCurrentInitiatorEvent(long userId, long eventId) {
        getUser(userId);
        Event event = getEvent(eventId);
        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = utils.getViews(Set.of(event));

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
        UpdateEventUserRequest.StateAction stateAction = dto.getStateAction();

        if (dto.getCategory() != null) {
            event.setCategory(getCat(dto.getCategory()));
        }

        completeFieldsForUpdated(dto, event);

        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = utils.getViews(Set.of(event));

        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(EVENT_NOT_AVAILABLE_STATE);
        }

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException(BAD_START_TIME);
        }

        if (stateAction != null) {
            if (stateAction.equals(UpdateEventUserRequest.StateAction.SEND_TO_REVIEW)) {
                event.setEventState(EventState.PENDING);
            } else if (stateAction.equals(UpdateEventUserRequest.StateAction.CANCEL_REVIEW)) {
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

        List<ParticipationRequest> requests = requestStorage.findParticipationRequestByEventId(eventId);
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
        EventRequestStatusUpdateRequest.Status status = requests.getStatus();
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

        if (status.toString().equals(Status.CONFIRMED.toString())) {
            return confirmRequests(ids, participationLimit, participationCount);
        } else if (status.toString().equals(Status.REJECTED.toString())) {
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
                rangeEnd, PageRequest.of(from / size, size, SortConstants.SORT_EVENT_BY_ID_DESC));
        Map<Long, Long> confirmed = getConfirmedRequests(events);
        Map<Long, Long> views = utils.getViews(new HashSet<>(events));

        return makeEventFull(events, confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEventStatusByAdmin(long eventId, UpdateEventAdminRequest dto) {
        Event event = getEvent(eventId);
        LocalDateTime eventDate = event.getEventDate();
        UpdateEventAdminRequest.StateAction stateAction = dto.getStateAction();

        completeFieldsForUpdated(dto, event);

        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = utils.getViews(Set.of(event));

        if (!eventDate.isAfter(LocalDateTime.now().plusHours(1L))) {
            throw new ConflictException(ErrorConstants.EVENT_START_TIME);
        }
        if (!event.getEventState().equals(EventState.PENDING)) {
            throw new ConflictException(WRONG_EVENT_STATUS_FOR_CONFIRM);
        }

        if (stateAction != null) {
            if (stateAction.equals(UpdateEventAdminRequest.StateAction.PUBLISH_EVENT)) {
                event.setEventState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                eventStorage.save(event);
            } else if (stateAction.equals(UpdateEventAdminRequest.StateAction.REJECT_EVENT)) {
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
            throw new NotAvailableException(WRONG_CONDITION);
        }

        if (sort != null && sort.equalsIgnoreCase("EVENT_DATE")) {
            currentSort = SortConstants.SORT_BY_EVENT_DATE_DESC;
        } else if (sort != null && sort.equalsIgnoreCase("VIEWS")) {
            currentSort = SortConstants.SORT_BY_VIEWS_DESC;
        } else {
            currentSort = SortConstants.SORT_EVENT_BY_ID_DESC;
        }

        List<Event> events = eventStorage.findEventByNotRegistrationUser(text, categories, isPaid, rangeStart, rangeEnd,
                onlyAvailable, PageRequest.of(from / size, size, currentSort));
        Map<Long, Long> confirmedRequest = getConfirmedRequests(events);
        Map<Long, Long> views = utils.getViews(new HashSet<>(events));

        return makeEventShort(events, confirmedRequest, views);
    }

    @Override
    public EventFullDto getEventForNotRegistrationUserById(long eventId) {
        Event event = getEvent(eventId);
        Map<Long, Long> confirmed = getConfirmedRequests(List.of(event));
        Map<Long, Long> views = utils.getViews(Set.of(event));

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

    private Event completeFieldsForUpdated(UpdateEventRequest dto, Event event) {
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
            event.setLocation(LocationMapper.makeLocation(dto.getLocation()));
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
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            event.setTitle(dto.getTitle());
        }
        return event;
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .collect(toList());

        return requestStorage.countByEvent(eventsIds).stream()
                .collect(toMap(EventConfirmedParticipation::getEventId, EventConfirmedParticipation::getCount));
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
