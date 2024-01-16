package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.state.EventState;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getInitiatorEvents(long userId, int from, int size);

    EventFullDto addEvent(long userId, NewEventDto newEventDto);

    EventFullDto getCurrentInitiatorEvent(long userId, long eventId);

    EventFullDto updateEventByInitiator(long userId, long eventId, UpdateEventUserRequest dto);

    List<ParticipationRequestDto> getEventsParticipationRequests(long userId, long eventId);

    EventRequestStatusUpdateResult confirmEventsParticipationRequests(long userId, long eventId,
                                                                      EventRequestStatusUpdateRequest requests);

    List<EventFullDto> getEventByAdminFiltering(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventStatusByAdmin(long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> getEventForNotRegistrationUserByFiltering(String text, Integer[] categories, Boolean isPaid,
                                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                                  Boolean onlyAvailable, String sort, int from, int size);

    EventFullDto getEventForNotRegistrationUserById(long eventId);
}
