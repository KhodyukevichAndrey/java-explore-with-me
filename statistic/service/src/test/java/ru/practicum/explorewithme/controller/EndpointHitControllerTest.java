package ru.practicum.explorewithme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.EndpointHitController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.service.EndpointHitServiceImpl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = EndpointHitController.class)
class EndpointHitControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private EndpointHitServiceImpl service;
    @Autowired
    private MockMvc mvc;

    private EndpointHitDto endpointHitDto;
    private EndpointHitStatsDto statsDto;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private final LocalDateTime start = LocalDateTime.of(2020, 8, 29,
            0, 0, 15);
    private final LocalDateTime end = LocalDateTime.of(2030, 8, 30,
            0, 0, 15);
    String encodedStart = start.format(FORMATTER);
    String encodedEnd = end.format(FORMATTER);

    @BeforeEach
    void createStatsControllerEnvironment() {
        endpointHitDto = new EndpointHitDto("appText", "uriText", "ipText", LocalDateTime.now());
        statsDto = new EndpointHitStatsDto(endpointHitDto.getApp(), endpointHitDto.getUri(), 1);
    }

    @Test
    void addNewHitAndThenOk() throws Exception {
        mvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void addNewHitAndThenThrowsBadRequest() throws Exception {
        endpointHitDto.setApp("");

        mvc.perform(post("/hit")
                        .content(objectMapper.writeValueAsString(endpointHitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEndpointHitStatsAndThenOk() throws Exception {
        when(service.getStats(any(), any(), any(), any())).thenReturn(List.of(statsDto));

        mvc.perform(get("/stats")
                        .param("start", encodedStart)
                        .param("end", encodedEnd)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getEndpointHitStatsWithoutOneParamAndThrowsBadRequest() throws Exception {
        when(service.getStats(any(), any(), any(), any())).thenReturn(List.of(statsDto));

        mvc.perform(get("/stats")
                        .param("start", start.toString()) // no end
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
