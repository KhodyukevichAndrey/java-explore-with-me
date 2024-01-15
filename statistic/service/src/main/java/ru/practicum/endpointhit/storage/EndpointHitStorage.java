package ru.practicum.endpointhit.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.EndpointHitStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitStorage extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.dto.EndpointHitStatsDto(eh.app, eh.uri, count(eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.dateTime BETWEEN ?1 AND ?2 " +
            "group by eh.uri, eh.app " +
            "order by count(eh.ip) desc")
    List<EndpointHitStatsDto> findAllEndpointHitByDate(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.EndpointHitStatsDto(eh.app, eh.uri, count(eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.dateTime BETWEEN ?1 AND ?2 " +
            "AND eh.uri in ?3 " +
            "group by eh.uri, eh.app " +
            "order by count(eh.ip) desc")
    List<EndpointHitStatsDto> findEndpointHitForUriIn(LocalDateTime start, LocalDateTime end, String[] uris);

    @Query("select new ru.practicum.dto.EndpointHitStatsDto(eh.app, eh.uri, count(distinct eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.dateTime BETWEEN ?1 AND ?2 " +
            "group by eh.uri, eh.app " +
            "order by count(eh.ip) desc")
    List<EndpointHitStatsDto> findAllEndpointHitForUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.EndpointHitStatsDto(eh.app, eh.uri, count(distinct eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.dateTime BETWEEN ?1 AND ?2 " +
            "AND eh.uri in ?3 " +
            "group by eh.uri, eh.app " +
            "order by count(eh.ip) desc")
    List<EndpointHitStatsDto> findEndpointHitForUriInAndUnique(LocalDateTime start, LocalDateTime end, String[] uris);
}
