package ru.practicum.event.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventStorage extends JpaRepository<Event, Long> {

    List<Event> findEventByInitiatorId(long userId, Pageable p);

    Event findEventByInitiatorIdAndId(long userId, long eventId);

    @Query("select e " +
            "from Event e " +
            "where e.id IN ?1 or ?1 is null " +
            "AND e.eventState IN ?2 or ?2 is null " +
            "AND e.category.id IN ?3 or ?3 is null " +
            "AND e.eventDate > ?4 or ?4 is null " +
            "AND e.eventDate < ?5 or ?5 is null")
    List<Event> findEventByAdminParameters(Integer[] usersId, String[] states, Integer[] categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable p);


    @Query("select e " +
            "from Event e " +
            "where e.eventState = 'PUBLISHED' " +
            "AND ((lower(e.annotation) like concat('%', lower(?1), '%')) or (lower(e.description) like concat('%', lower(?1), '%')) or (?1 is null)) " +
            "AND ((e.category.id IN ?2) or (?2 is null)) " +
            "AND ((e.isPaid = ?3) or (?3 is null)) " +
            "AND (e.eventDate between ?4 and ?5) " +
            "AND (?6 = true AND e.participantLimit > (select count(pr) from ParticipationRequest as pr " +
            "where e.id = pr.event.id)) or ?6 = false")
    List<Event> findEventByNotRegistrationUser(String text, Integer[] categories, Boolean isPaid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable p);

    List<Event> findEventByIdIn(List<Long> ids);
}
