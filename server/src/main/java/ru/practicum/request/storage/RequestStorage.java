package ru.practicum.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    List<ParticipationRequest> findParticipationRequestByEventIdInAndStatus(List<Long> eventIds, Status status);
}
