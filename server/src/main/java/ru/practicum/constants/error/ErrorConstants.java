package ru.practicum.constants.error;

public class ErrorConstants {
    private ErrorConstants() {
    }

    public static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";
    public static final String WRONG_CAT_ID = "Категория с указанным ID не найдена";
    public static final String WRONG_EVENT_ID = "Событие с указанным ID не найдено";
    public static final String WRONG_REQUEST_ID = "Запрос на участие в событии с указанным ID не найден";
    public static final String WRONG_COMBINATION_OF_INIT_AND_EVENT =
            "Пользователь не является ораганизатором найденного мероприятия";
    public static final String WRONG_STATUS = "Инициатор события может либо подтвердить, либо отклонить заявку";
    public static final String WRONG_COMPILATION_ID = "Подборка событий по указанному ID не найдена";
    public static final String OUT_OF_LIMIT = "Превышен лимит участников события";
    public static final String WRONG_PARTICIPATION_STATUS = "Изменения статуса запроса на участие в " +
            "событии допустимо только для PENDING";
    public static final String EVENT_START_TIME = "дата начала изменяемого события должна быть не ранее," +
            " чем за час от даты публикации";
    public static final String EVENT_NOT_AVAILABLE_STATE = "Событие в текущем статусе не может быть изменено";
    public static final String DUPLICATION = "Вы уже направили запрос на участие в данном событии";
    public static final String PARTICIPATION_CONFLICT = "Оранизатор события не может направлять " +
            "запрос на принятие в нем участия";
    public static final String EVENT_NOT_PUBLISHED = "Невозможно принять участие в неопубликованном событии";
    public static final String OUT_OF_LIMIT_PARTICIPATION = "В событии уже принимает участие" +
            " максимальное кол-во участников";
    public static final String BAD_REQUEST = "Некорректно составленный запрос к серверу";
    public static final String NOT_FOUND = "Данные на основании полученных параметров не найдены";
    public static final String CONFLICT = "Конфликт запроса с текущим состоянием сервера";
    public static final String INTERNAL_SERVER_ERROR = "Неожиданная ошибка сервера";
    public static final String BAD_START_TIME = "дата и время на которые намечено событие не может быть раньше," +
            " чем через два часа от текущего момента";
    public static final String EVENT_CATEGORY_EXIST = "Удаление категории, для которой существует событие - недопустимо";
    public static final String WRONG_EVENT_STATUS_FOR_CONFIRM = "Согласование события из данного статуса - недопустимо";
    public static final String WRONG_CONDITION = "Дата начала не должна быть позже даты окончания";
    public static final int ID_START_FROM = 8;
    public static final String PRIVATE_ACCOUNT = "Пользователь ограничил возможность подписки на себя";
    public static final String WRONG_SUBSTATUS = "Подписка в подходящем статусе отсутствует";
    public static final String ALREADY_CONFIRMED = "Запрос уже подтвержден";
    public static final String ALREADY_REJECTED = "Запрос уже отклонен";
    public static final String NO_SUB_REQUESTS = "У вас нет подписок ожидающих обработки";
    public static final String SUB_REQUEST_DID_NOT_EXIST = "Запрос не найден";
}
