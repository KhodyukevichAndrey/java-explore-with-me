package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotNull
    @NotEmpty
    private Long[] requestIds;
    @NotNull
    private Status status;

    public enum Status {
        CONFIRMED,
        REJECTED
    }
}
