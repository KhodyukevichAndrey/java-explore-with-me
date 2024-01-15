package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.model.Category;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {

    private String annotation;
    private Category category;
    private int confirmedRequests;
    private LocalDateTime eventDate;
    private long id;
    private UserShortDto initiator;
    private boolean paid;
    private String title;
    private int views;
}
