package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.model.Location;

@UtilityClass
public class LocationMapper {

    public Location makeLocation(LocationDto locationDto) {
        return new Location(
                locationDto.getLat(),
                locationDto.getLon()
        );
    }

    public LocationDto makeLocationDto(Location location) {
        return new LocationDto(
                location.getLat(),
                location.getLon()
        );
    }
}
