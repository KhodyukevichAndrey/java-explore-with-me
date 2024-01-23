package ru.practicum.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

@UtilityClass
public class UserMapper {
    public User makeUser(NewUserRequest newUserRequest) {
        return new User(
                0,
                newUserRequest.getEmail(),
                newUserRequest.getName(),
                newUserRequest.isPublic()
        );
    }

    public UserDto makeUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.isPublic()
        );
    }

    public UserShortDto makeUserShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }
}
