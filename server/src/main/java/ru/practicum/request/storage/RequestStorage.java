package ru.practicum.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.request.model.EventConfirmedParticipation;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.status.Status;

import java.util.List;

public interface RequestStorage extends JpaRepository<ParticipationRequest, Long> {

    boolean existsByParticipantIdAndEventId(long userId, long eventId);

    List<ParticipationRequest> findAllByParticipantId(long userId);

    List<ParticipationRequest> findParticipationRequestByEventId(long eventId);

    List<ParticipationRequest> findParticipationRequestByIdIn(Long[] ids);

    @Query("select Count(pr.id) " +
            "from ParticipationRequest pr " +
            "where pr.event.id = ?1 " +
            "AND pr.status = ?2")
    Long getCountOfParticipation(long eventId, Status status);

    @Query("select new ru.practicum.request.model.EventConfirmedParticipation(pr.event.id, count(pr.id)) " +
            "from ParticipationRequest pr " +
            "where pr.status = 'CONFIRMED' " +
            "AND pr.event.id IN :ids " +
            "group by pr.event.id")
    List<EventConfirmedParticipation> countByEvent(@Param("ids") List<Long> ids);
}
