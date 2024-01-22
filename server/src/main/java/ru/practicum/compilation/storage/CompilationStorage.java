package ru.practicum.compilation.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

public interface CompilationStorage extends JpaRepository<Compilation, Long> {

    List<Compilation> findByPinned(boolean pinned, Pageable p);

    @Query("select c " +
            "from Compilation c " +
            "LEFT JOIN FETCH c.events e ")
    List<Compilation> findAllWithFetchedEvents(Pageable p); //TODO пока не работает, в процессе изучения
}
