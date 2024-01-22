package ru.practicum.subscription.model;

import lombok.*;
import ru.practicum.subscription.substatus.SubStatus;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime created;
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "subscriber_id")
    private User subscriber;
    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "sub_status")
    private SubStatus subStatus;
}
