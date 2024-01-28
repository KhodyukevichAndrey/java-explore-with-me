package ru.practicum.subscription.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.subscription.model.Subscription;
import ru.practicum.subscription.substatus.SubStatus;

import java.util.List;

public interface SubStorage extends JpaRepository<Subscription, Long> {

    Subscription findSubscriptionBySubscriberIdAndInitiatorId(long subscriberId, long initiatorId);

    List<Subscription> findSubscriptionByInitiatorIdAndSubStatus(long initiatorId, SubStatus subStatus);

    List<Subscription> findSubscriptionBySubscriberIdAndSubStatus(long initiatorId, SubStatus subStatus);
}
