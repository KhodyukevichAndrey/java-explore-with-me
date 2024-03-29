package ru.practicum.event.model;

import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.event.state.EventState;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator")
    @ToString.Exclude
    private User initiator;
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "location_lon"))})
    private Location location;
    @Column(name = "is_paid", nullable = false, columnDefinition = "boolean default false")
    private Boolean isPaid;
    @Column(name = "participant_limit", nullable = false, columnDefinition = "long default 0L")
    private Long participantLimit;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation", nullable = false, columnDefinition = "boolean default true")
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState eventState;
    private String title;
}
