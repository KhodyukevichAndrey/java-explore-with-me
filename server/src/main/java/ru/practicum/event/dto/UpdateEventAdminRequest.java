package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.state.StateAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest extends NewEventDto{
    private StateAction stateAction;
}
