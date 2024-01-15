package ru.practicum.user.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserStorage extends JpaRepository<User, Long> {

    @Query("select u " +
            "from User u " +
            "where u.id IN ?1 or ?1 is null " +
            "order by u.id ASC")
    List<User> findAllUserByIdIn(Integer[] ids, Pageable p);
}
