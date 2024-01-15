package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.status.Status;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class RequestMapper {

    public ParticipationRequest makeRequest(User participant, Event event) {
        return new ParticipationRequest(
                0,
                participant,
                event,
                Status.PENDING
        );
    }

    public ParticipationRequestDto makeRequestDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                LocalDateTime.now(),
                request.getEvent().getId(),
                request.getParticipant().getId(),
                request.getStatus()
        );
    }
}
