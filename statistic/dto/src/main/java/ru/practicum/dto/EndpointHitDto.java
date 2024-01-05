package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class EndpointHitDto {
    @NotBlank
    @Size(max = 50)
    private String app;
    @NotBlank
    @Size(max = 50)
    private String uri;
    @NotBlank
    @Size(max = 50)
    private String ip;
}
