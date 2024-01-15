package ru.practicum.request.model;

import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.status.Status;
import ru.practicum.user.model.User;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requests")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "user_id")
    private User participant;
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "event_id")
    private Event event;
    @Enumerated(EnumType.STRING)
    private Status status;
}
