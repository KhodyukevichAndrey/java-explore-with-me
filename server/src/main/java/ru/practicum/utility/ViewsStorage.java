package ru.practicum.utility;
import ru.practicum.event.model.Event;
import java.util.*;

public interface ViewsStorage {

    Map<Long, Long> getViews(Set<Event> events);
}
