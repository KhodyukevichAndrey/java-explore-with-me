package ru.practicum.user.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserStorage extends JpaRepository<User, Long> {

    @Query("select u " +
            "from User u " +
            "where (u.id IN :ids or :ids is null)")
    List<User> findAllUserByIdIn(@Param("ids") List<Long> ids, Pageable p);
}
