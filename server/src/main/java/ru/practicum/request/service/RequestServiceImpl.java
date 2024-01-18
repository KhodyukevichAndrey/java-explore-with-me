package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.EventConfirmedParticipation;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.status.Status;
import ru.practicum.request.storage.RequestStorage;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static ru.practicum.constants.error.ErrorConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestStorage requestStorage;
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        User user = getUser(userId);
        Event event = getEvent(eventId);
        ParticipationRequest request = RequestMapper.makeRequest(user, event);
        Map<Long, Long> confirmedRequest = getConfirmedRequests(List.of(event));
        long limit = event.getParticipantLimit();

        if (requestStorage.existsByParticipantIdAndEventId(userId, eventId)) {
            throw new ConflictException(DUPLICATION);
        }
        if (event.getInitiator().getId() == userId) {
            throw new ConflictException(PARTICIPATION_CONFLICT);
        }
        if (!event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(EVENT_NOT_PUBLISHED);
        }
        if (limit != 0 && confirmedRequest.getOrDefault(eventId, 0L) == limit) {
            throw new ConflictException(OUT_OF_LIMIT_PARTICIPATION);
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }

        return RequestMapper.makeRequestDto(requestStorage.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUsersRequests(long userId) {
        getUser(userId);
        List<ParticipationRequest> requests = requestStorage.findAllByParticipantId(userId);

        return requests.stream()
                .map(RequestMapper::makeRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelUserRequest(long userId, long requestId) {
        getUser(userId);
        ParticipationRequest request = getRequest(requestId);
        request.setStatus(Status.CANCELED);

        return RequestMapper.makeRequestDto(requestStorage.save(request));
    }

    private User getUser(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    private Event getEvent(long eventId) {
        return eventStorage.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_EVENT_ID));
    }

    private ParticipationRequest getRequest(long requestId) {
        return requestStorage.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_REQUEST_ID));
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .collect(toList());

        return requestStorage.countByEvent(eventsIds).stream()
                .collect(toMap(EventConfirmedParticipation::getEventId, EventConfirmedParticipation::getCount));
    }
}
