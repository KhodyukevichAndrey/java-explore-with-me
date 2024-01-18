package ru.practicum.event.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.state.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventStorage extends JpaRepository<Event, Long> {

    List<Event> findEventByInitiatorId(long userId, Pageable p);

    Event findEventByInitiatorIdAndId(long userId, long eventId);

    @Query("select e " +
            "from Event e " +
            "where (e.initiator.id IN :usersId or :usersId is null) " +
            "AND (e.eventState IN :states or :states is null) " +
            "AND (e.category.id IN :categories or :categories is null) " +
            "AND (e.eventDate < cast(:rangeEnd AS date) or cast(:rangeStart AS date) is null) " +
            "AND (e.eventDate > cast(:rangeStart AS date) or cast(:rangeEnd AS date) is null) ")
    List<Event> findEventByAdminParameters(@Param("usersId") List<Long> usersId,
                                           @Param("states") List<EventState> states,
                                           @Param("categories") List<Long> categories,
                                           @Param("rangeStart") LocalDateTime rangeStart,
                                           @Param("rangeEnd") LocalDateTime rangeEnd,
                                           Pageable p);


    @Query("select e " +
            "from Event e " +
            "where e.eventState = 'PUBLISHED' " +
            "AND ((lower(e.annotation) like lower(concat('%', :text, '%'))) " +
            "or (lower(e.description) like lower(concat('%', :text, '%'))) " +
            "or (:text is null)) " +
            "AND (e.category.id IN :categories or :categories is null) " +
            "AND (e.isPaid = :isPaid or :isPaid is null) " +
            "AND (e.eventDate < cast(:rangeEnd AS date) or cast(:rangeStart AS date) is null) " +
            "AND (e.eventDate > cast(:rangeStart AS date) or cast(:rangeEnd AS date) is null) " +
            "AND (:onlyAvailable = false OR ((:onlyAvailable = true AND e.participantLimit > " +
            "(SELECT count(*) FROM ParticipationRequest pr WHERE e.id = pr.event.id))) " +
            "OR (e.participantLimit > 0 ))")
    List<Event> findEventByNotRegistrationUser(@Param("text") String text,
                                               @Param("categories") List<Long> categories,
                                               @Param("isPaid") Boolean isPaid,
                                               @Param("rangeStart") LocalDateTime rangeStart,
                                               @Param("rangeEnd") LocalDateTime rangeEnd,
                                               @Param("onlyAvailable") Boolean onlyAvailable,
                                               Pageable p);

    Set<Event> findEventByIdIn(Set<Long> ids);

    boolean existsEventByCategoryId(long catId);
}
