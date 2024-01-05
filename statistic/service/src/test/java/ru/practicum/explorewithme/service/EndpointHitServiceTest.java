package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.service.EndpointHitServiceImpl;
import ru.practicum.storage.EndpointHitStorage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointHitServiceTest {

    @Mock
    private EndpointHitStorage storage;
    @InjectMocks
    private EndpointHitServiceImpl service;

    private final LocalDateTime start = LocalDateTime.of(2020, 8, 29,
            0, 0, 15);
    private final LocalDateTime end = LocalDateTime.of(2030, 8, 30,
            0, 0, 15);
    String encodedStart = URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8);
    String encodedEnd = URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

    private EndpointHitDto endpointHitDto;
    private EndpointHitStatsDto statsDto;
    private EndpointHit endpointHit;
    private String[] uris = new String[]{"uriText"};
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void createStatsControllerEnvironment() {
        endpointHitDto = new EndpointHitDto("appText", "uriText", "ipText");
        endpointHit = EndpointHitMapper.makeEndpointHit(endpointHitDto);
        statsDto = new EndpointHitStatsDto(endpointHitDto.getApp(), endpointHitDto.getUri(), 1);
    }

    @Test
    void addNewHitAndThenOk() {
        when(storage.save(any())).thenReturn(endpointHit);

        EndpointHitDto currentDto = service.addCallEndpointHit(endpointHitDto);

        assertThat(currentDto.getApp(), equalTo(endpointHit.getApp()));
        assertThat(currentDto.getIp(), equalTo(endpointHit.getIp()));
        assertThat(currentDto.getUri(), equalTo(endpointHit.getUri()));
    }

    @Test
    void getStatsWhenEndIsBeforeStartAndThenOkWithEmptyList() {
        List<EndpointHitStatsDto> stats = service.getStats(encodedEnd, encodedStart, null, null);

        assertThat(stats.size(), equalTo(0));
    }

    @Test
    void getStatsWithUrisAndThenOk() {
        when(storage.findEndpointHitForUriIn(any(), any(), any())).thenReturn(List.of(statsDto));

        List<EndpointHitStatsDto> stats = service.getStats(encodedStart, encodedEnd, uris, null);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsWithUrisAndUniqueThenOk() {
        when(storage.findEndpointHitForUriInAndUnique(any(), any(), any())).thenReturn(List.of(statsDto));

        List<EndpointHitStatsDto> stats = service.getStats(encodedStart, encodedEnd, uris, true);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsWithoutUrisAndWithUniqueThenOk() {
        when(storage.findAllEndpointHitForUnique(any(), any())).thenReturn(List.of(statsDto));

        List<EndpointHitStatsDto> stats = service.getStats(encodedStart, encodedEnd, null, true);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsWithDatesOnlyAndThenOk() {
        when(storage.findAllEndpointHitByDate(any(), any())).thenReturn(List.of(statsDto));

        List<EndpointHitStatsDto> stats = service.getStats(encodedStart, encodedEnd, null, null);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }
}
