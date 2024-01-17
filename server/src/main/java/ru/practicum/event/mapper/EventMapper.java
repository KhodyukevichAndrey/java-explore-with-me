package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public Event makeEvent(NewEventDto dto, User initiator, Category category) {
        return new Event(
                0,
                dto.getAnnotation(),
                category,
                LocalDateTime.now(),
                dto.getDescription(),
                dto.getEventDate(),
                initiator,
                dto.getLocation(),
                dto.getPaid(),
                dto.getParticipantLimit(),
                LocalDateTime.now(), //обновление после подтверждения события
                dto.getRequestModeration(),
                EventState.PENDING,
                dto.getTitle()
        );
    }

    public EventFullDto makeEventFullDto(Event event, long confirmedRequest, long views) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.makeCatDto(event.getCategory()),
                confirmedRequest,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                UserMapper.makeUserShortDto(event.getInitiator()),
                event.getLocation(),
                event.getIsPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getEventState(),
                event.getTitle(),
                views
        );
    }

    public EventShortDto makeEventShortDto(Event event, long confirmedRequest, long views) {
        return new EventShortDto(
                event.getAnnotation(),
                event.getCategory(),
                confirmedRequest,
                event.getEventDate(),
                event.getId(),
                UserMapper.makeUserShortDto(event.getInitiator()),
                event.getIsPaid(),
                event.getTitle(),
                views
        );
    }
}
