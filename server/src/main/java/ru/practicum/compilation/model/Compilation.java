package ru.practicum.compilation.model;

import lombok.*;
import org.hibernate.annotations.BatchSize;
import ru.practicum.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JoinTable(name = "compilations_events",
            joinColumns = {@JoinColumn(name = "compilation_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")})
    @BatchSize(size = 10)
    private Set<Event> events;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean pinned;
    private String title;
}
