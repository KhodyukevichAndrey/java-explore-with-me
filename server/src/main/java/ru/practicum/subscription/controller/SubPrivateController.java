package ru.practicum.subscription.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.subscription.dto.SubShortDto;
import ru.practicum.subscription.service.SubService;
import ru.practicum.user.dto.UserShortDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class SubPrivateController {

    private final SubService subService;

    @PostMapping("/subscriber/{subscriberId}/{initiatorId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SubShortDto createSubRequest(@PathVariable long subscriberId, @PathVariable long initiatorId) {
        log.debug("Получен запрос Post /users/subscriptions/subscriber/{subscriberId}/{initiatorId}");
        return subService.createSubRequest(subscriberId, initiatorId);
    }

    @DeleteMapping("/subscriber/{subscriberId}/{initiatorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSubscription(@PathVariable long subscriberId, @PathVariable long initiatorId) {
        log.debug("Получен запрос Delete /users/subscriptions/subscriber/{subscriberId}/{initiatorId}");
        subService.cancelSubRequestBySubscriber(subscriberId, initiatorId);
    }

    @PatchMapping("/author/{initiatorId}/{subscriberId}")
    public SubShortDto confirmSubRequest(@PathVariable long initiatorId, @PathVariable long subscriberId,
                                         @RequestParam Boolean isConfirm) {
        log.debug("Получен запрос Patch /users/subscriptions/author/{initiatorId}/{subscriberId}?isConfirm={}", isConfirm);
        return subService.changeStatusForSubscriptionByInitiator(initiatorId, subscriberId, isConfirm);
    }

    @GetMapping("/author/{initiatorId}")
    public List<UserShortDto> getMyFollowers(@PathVariable long initiatorId) {
        log.debug("Получен запрос Get /users/subscriptions/author/{initiatorId}");
        return subService.getMySubRequests(initiatorId);
    }

    @GetMapping("/subscriber/{subscriberId}")
    public List<UserShortDto> getMyEventAuthors(@PathVariable long subscriberId) {
        log.debug("Получен запрос Get /users/subscriptions/subscriber/{subscriberId}");
        return subService.getMyEventAuthors(subscriberId);
    }

    @PatchMapping("/author/{initiatorId}")
    public List<SubShortDto> confirmAllSubRequest(@PathVariable long initiatorId, @RequestParam Boolean isConfirm) {
        log.debug("Получен запрос Patch /users/subscriptions/author/{initiatorId}");
        return subService.changeStatusForAllSubRequests(initiatorId, isConfirm);
    }

    @GetMapping("/subscriber/search/{subscriberId}")
    public List<EventShortDto> getEventsForSubscriber(@RequestParam(required = false) String text,
                                                      @RequestParam(required = false) List<Long> categories,
                                                      @RequestParam(required = false) Boolean paid,
                                                      @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                      @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                      @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                      @RequestParam(defaultValue = "10") @Positive int size,
                                                      @PathVariable long subscriberId) {
        log.debug("Получен запрос Get /users/subscriptions/subscriber/search/{subscriberId}?text={}&categories={}&paid={}" +
                        "&rangeStart={}&rangeEnd={}&onlyAvailable={}&sort={}&from={}&size={}", text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, size, from, size);
        return subService.getEventsForSubscriber(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, from, size, subscriberId);
    }
}
