package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UpdateUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.constants.error.ErrorConstants.WRONG_USER_ID;
import static ru.practicum.constants.sort.SortConstants.SORT_USERS_BY_ID_ASC;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserStorage storage;

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        User user = storage.save(UserMapper.makeUser(newUserRequest));
        return UserMapper.makeUserDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users;
        PageRequest pr = PageRequest.of(from / size, size, SORT_USERS_BY_ID_ASC);
        if (ids != null) {
            users = storage.findAllUserByIdIn(ids, pr);
        } else {
            users = storage.findAll(pr).getContent();
        }
        return users.stream()
                .map(UserMapper::makeUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        getUser(userId);
        storage.deleteById(userId);
    }

    @Override
    @Transactional
    public UserDto updateUserByUser(long userId, UpdateUserDto updateUserDto) {
        User user = getUser(userId);

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isBlank()) {
            user.setEmail(updateUserDto.getEmail());
        }

        if (updateUserDto.getName() != null && !updateUserDto.getName().isBlank()) {
            user.setName(updateUserDto.getName());
        }

        if (updateUserDto.getIsPublic() != null && updateUserDto.getIsPublic() != user.isPublic()) {
            user.setPublic(updateUserDto.getIsPublic());
        }

        return UserMapper.makeUserDto(storage.save(user));
    }

    private User getUser(long userId) {
        return storage.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }
}
