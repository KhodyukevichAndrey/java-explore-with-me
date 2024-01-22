package ru.practicum.subscription.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.subscription.dto.SubShortDto;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.substatus.SubStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class SubMapper {

    public Subscription makeSub(User subscriber, User initiator, SubStatus subStatus) {
        return new Subscription(
                0,
                LocalDateTime.now(),
                subscriber,
                initiator,
                subStatus
        );
    }

    public SubShortDto makeSubShortDto(Subscription subscription) {
        return new SubShortDto(
                subscription.getId(),
                subscription.getCreated(),
                subscription.getSubscriber().getId(),
                subscription.getInitiator().getId(),
                subscription.getSubStatus()
        );
    }
}
