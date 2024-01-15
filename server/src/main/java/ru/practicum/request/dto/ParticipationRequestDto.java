package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.status.Status;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private long id;
    private LocalDateTime created;
    private long event;
    private long requester;
    private Status status;
}
