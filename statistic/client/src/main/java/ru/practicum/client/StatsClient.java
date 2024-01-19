package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${explore-with-me-statistic.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postEndpointHit(EndpointHitDto dto) {
        return post("/hit", dto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, @Nullable String[] uris, @Nullable Boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start.format(FORMATTER));
        parameters.put("end", end.format(FORMATTER));

        StringBuilder builder = new StringBuilder();
        builder.append("/stats?start={start}&end={end}");

        if (uris != null) {
            parameters.put("uris", String.join(",", uris));
            builder.append("&uris={uris}");
        }

        if (unique != null) {
            parameters.put("unique", unique);
            builder.append("&unique={unique}");
        }

        return get(builder.toString(), parameters);
    }
}