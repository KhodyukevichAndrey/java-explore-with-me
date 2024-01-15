package ru.practicum.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.endpointhit.storage.EndpointHitStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class EndpointHitStorageTest {

    @Autowired
    private TestEntityManager tem;
    @Autowired
    private EndpointHitStorage storage;

    private EndpointHitDto endpointHitDto;
    private EndpointHitStatsDto statsDto;
    private EndpointHit endpointHit;
    private String[] uris = new String[]{"uriText"};
    private final LocalDateTime start = LocalDateTime.of(2020, 8, 29,
            0, 0, 15);
    private final LocalDateTime end = LocalDateTime.of(2030, 8, 30,
            0, 0, 15);

    @BeforeEach
    void createStatsControllerEnvironment() {
        endpointHitDto = new EndpointHitDto("appText", "uriText", "ipText", LocalDateTime.now());
        endpointHit = EndpointHitMapper.makeEndpointHit(endpointHitDto);
        statsDto = new EndpointHitStatsDto(endpointHitDto.getApp(), endpointHitDto.getUri(), 1);
    }

    @Test
    void verifyCreateEndpointHit() {
        EndpointHit eh = tem.persist(endpointHit);

        assertEquals(eh.getId(), 1);
    }

    @Test
    void getStatsWithUrisAndThenOk() {
        tem.persist(endpointHit);

        List<EndpointHitStatsDto> stats = storage.findEndpointHitForUriIn(start, end, uris);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsWithUrisAndUniqueThenOk() {
        tem.persist(endpointHit);

        List<EndpointHitStatsDto> stats = storage.findEndpointHitForUriInAndUnique(start, end, uris);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsWithoutUrisAndWithUniqueThenOk() {
        tem.persist(endpointHit);

        List<EndpointHitStatsDto> stats = storage.findAllEndpointHitForUnique(start, end);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsWithDatesOnlyAndThenOk() {
        tem.persist(endpointHit);

        List<EndpointHitStatsDto> stats = storage.findAllEndpointHitByDate(start, end);

        assertThat(stats.size(), equalTo(1));
        assertThat(stats.get(0).getUri(), equalTo(statsDto.getUri()));
        assertThat(stats.get(0).getApp(), equalTo(statsDto.getApp()));
        assertThat(stats.get(0).getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    void getStatsForPeriodWithoutEndpointHitsAndThenOk() {
        tem.persist(endpointHit);

        List<EndpointHitStatsDto> stats = storage.findAllEndpointHitByDate(start.plusYears(6), end);

        assertThat(stats.size(), equalTo(0));
    }
}
