package ru.practicum.subscription.service;

import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.dto.SubShortDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SubService {
    SubShortDto createSubRequest(long subscriberId, long initiatorId);

    void cancelSubRequestBySubscriber(long subscriberId, long initiatorId);

    SubShortDto changeStatusForSubscriptionByInitiator(long initiatorId, long subscriberId, boolean isConfirm);

    List<UserShortDto> getMySubRequests(long initiatorId);

    List<UserShortDto> getMyEventAuthors(long subscriberId);

    List<SubShortDto> changeStatusForAllSubRequests(long initiatorId, boolean isConfirm);

    List<EventShortDto> getEventsForSubscriber(String text, List<Long> categories, Boolean isPaid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, int from, int size,
                                               long subscriberId);
}
