package ru.practicum.subscription.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventStorage;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.request.model.EventConfirmedParticipation;
import ru.practicum.request.storage.RequestStorage;
import ru.practicum.subscription.dto.SubShortDto;
import ru.practicum.subscription.mapper.SubMapper;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.storage.SubStorage;
import ru.practicum.subscription.substatus.SubStatus;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserStorage;
import ru.practicum.utility.Utils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ru.practicum.constants.error.ErrorConstants.*;
import static ru.practicum.constants.sort.SortConstants.SORT_EVENT_BY_ID_DESC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubServiceImpl implements SubService {

    private final SubStorage subStorage;
    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final RequestStorage requestStorage;
    private final Utils utils;

    @Override
    @Transactional
    public SubShortDto createSubRequest(long subscriberId, long initiatorId) {
        User subscriber = getUser(subscriberId);
        User initiator = getUser(initiatorId);

        if (!initiator.isPublic()) {
            throw new ConflictException(PRIVATE_ACCOUNT);
        }

        Subscription sub = subStorage.save(SubMapper.makeSub(subscriber, initiator, SubStatus.PENDING));

        return SubMapper.makeSubShortDto(sub);
    }

    @Override
    @Transactional
    public void cancelSubRequestBySubscriber(long subscriberId, long initiatorId) {
        getUser(initiatorId);
        getUser(subscriberId);

        Subscription sub = subStorage.findSubscriptionBySubscriberIdAndInitiatorId(subscriberId, initiatorId);
        if (sub == null) {
            throw new EntityNotFoundException(SUB_REQUEST_DID_NOT_EXIST);
        }
        SubStatus subStatus = sub.getSubStatus();

        if (subStatus.equals(SubStatus.REJECTED)) {
            throw new ConflictException(WRONG_SUBSTATUS);
        }

        subStorage.deleteById(sub.getId());
    }

    @Override
    @Transactional
    public SubShortDto changeStatusForSubscriptionByInitiator(long initiatorId, long subscriberId, boolean isConfirm) {
        getUser(initiatorId);
        getUser(subscriberId);

        Subscription sub = subStorage.findSubscriptionBySubscriberIdAndInitiatorId(subscriberId, initiatorId);
        SubStatus currentSubStatus = sub.getSubStatus();

        if (isConfirm) {
            if (currentSubStatus.equals(SubStatus.CONFIRMED)) {
                throw new ConflictException(ALREADY_CONFIRMED);
            }
            sub.setSubStatus(SubStatus.CONFIRMED);
        } else {
            if (currentSubStatus.equals(SubStatus.REJECTED)) {
                throw new ConflictException(ALREADY_REJECTED);
            }
            sub.setSubStatus(SubStatus.REJECTED);
        }

        return SubMapper.makeSubShortDto(subStorage.save(sub));
    }

    @Override
    public List<UserShortDto> getMySubRequests(long initiatorId) {
        getUser(initiatorId);

        List<Subscription> subRequests = subStorage.findSubscriptionByInitiatorIdAndSubStatus(initiatorId, SubStatus.PENDING);

        return subRequests.stream()
                .map(Subscription::getSubscriber)
                .map(UserMapper::makeUserShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserShortDto> getMyEventAuthors(long subscriberId) {
        getUser(subscriberId);

        List<Subscription> confirmedEventSubs =
                subStorage.findSubscriptionBySubscriberIdAndSubStatus(subscriberId, SubStatus.CONFIRMED);

        return confirmedEventSubs.stream()
                .map(Subscription::getInitiator)
                .map(UserMapper::makeUserShortDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public List<SubShortDto> changeStatusForAllSubRequests(long initiatorId, boolean isConfirm) {
        getUser(initiatorId);

        List<Subscription> subs = subStorage.findSubscriptionByInitiatorIdAndSubStatus(initiatorId, SubStatus.PENDING);
        if (subs.isEmpty()) {
            throw new ConflictException(NO_SUB_REQUESTS);
        }
        for (Subscription s : subs) {
            if (isConfirm) {
                s.setSubStatus(SubStatus.CONFIRMED);
            } else {
                s.setSubStatus(SubStatus.REJECTED);
            }
        }
        subStorage.saveAll(subs);

        return subs.stream()
                .map(SubMapper::makeSubShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsForSubscriber(String text, List<Long> categories, Boolean isPaid,
                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                      Boolean onlyAvailable, int from, int size,
                                                      long subscriberId) {

        List<Event> eventsByInitiator = eventStorage.findFilterEventByInitiatorIdIn(text, categories, isPaid, rangeStart, rangeEnd,
                onlyAvailable, PageRequest.of(from / size, size, SORT_EVENT_BY_ID_DESC), subscriberId);
        Map<Long, Long> confirmed = getConfirmedRequests(eventsByInitiator);
        Map<Long, Long> views = utils.getViews(new HashSet<>(eventsByInitiator));


        return makeEventShort(eventsByInitiator, confirmed, views);
    }

    private User getUser(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .collect(toList());

        return requestStorage.countByEvent(eventsIds).stream()
                .collect(toMap(EventConfirmedParticipation::getEventId, EventConfirmedParticipation::getCount));
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
