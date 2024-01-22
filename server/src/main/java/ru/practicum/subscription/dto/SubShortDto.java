package ru.practicum.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.subscription.substatus.SubStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubShortDto {
    private long id;
    private LocalDateTime created;
    private long subscriberId;
    private long initiatorId;
    private SubStatus subStatus;
}
