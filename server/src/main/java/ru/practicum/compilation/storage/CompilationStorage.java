package ru.practicum.compilation.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

public interface CompilationStorage extends JpaRepository<Compilation, Long> {

    @Query("select c " +
            "from Compilation c " +
            "LEFT JOIN FETCH c.events e " +
            "where c.pinned = :pinned ")
    List<Compilation> findByPinned(@Param("pinned") boolean pinned, Pageable p);

    @Query("select c " +
            "from Compilation c " +
            "LEFT JOIN FETCH c.events e ")
    List<Compilation> findAllWithFetchedEvents(Pageable p);
}
