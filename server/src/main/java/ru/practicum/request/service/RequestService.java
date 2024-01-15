package ru.practicum.request.service;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto addRequest(long userId, long eventId);

    List<ParticipationRequestDto> getUsersRequests(long userId);

    ParticipationRequestDto cancelUserRequest(long userId, long requestId);
}
