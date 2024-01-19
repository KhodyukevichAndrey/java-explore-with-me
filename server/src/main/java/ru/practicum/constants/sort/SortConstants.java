package ru.practicum.constants.sort;

import org.springframework.data.domain.Sort;

public class SortConstants {

    public static final Sort SORT_EVENT_BY_ID_DESC = Sort.by(Sort.Direction.DESC, "id");
    public static final Sort SORT_USERS_BY_ID_ASC = Sort.by(Sort.Direction.ASC, "id");
    public static final Sort SORT_BY_EVENT_DATE_DESC = Sort.by(Sort.Direction.DESC, "eventDate");
    public static final Sort SORT_BY_VIEWS_DESC = Sort.by(Sort.Direction.DESC, "views");
}
