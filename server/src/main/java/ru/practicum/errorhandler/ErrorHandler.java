package ru.practicum.errorhandler;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.NotAvailableException;

import java.time.LocalDateTime;
import java.util.Arrays;

import static ru.practicum.constants.error.ErrorConstants.*;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final RuntimeException e) {
        log.debug("Получен статус 409 Conflict {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), CONFLICT,
                "CONFLICT", LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentException(final MethodArgumentNotValidException e) {
        log.debug("Получен статус 400 Bad Request {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), BAD_REQUEST,
                "BAD_REQUEST", LocalDateTime.now());
    }

    @ExceptionHandler(NotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNotAvailableException(final RuntimeException e) {
        log.debug("Получен статус 400 Bad Request {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), BAD_REQUEST,
                "BAD_REQUEST", LocalDateTime.now());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterExceptionException(final MissingServletRequestParameterException e) {
        log.debug("Получен статус 400 Bad Request {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), BAD_REQUEST,
                "BAD_REQUEST", LocalDateTime.now());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final RuntimeException e) {
        log.debug("Получен статус 404 Not found {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), NOT_FOUND,
                "NOT_FOUND", LocalDateTime.now());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final RuntimeException e) {
        log.debug("Получен статус 409 Conflict {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), CONFLICT,
                "CONFLICT", LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUndefinedException(final Throwable e) {
        log.debug("Получен статус 500 Internal Server Error {}", e.getMessage(), e);
        return new ApiError(Arrays.toString(e.getStackTrace()), e.getMessage(), INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR", LocalDateTime.now());
    }
}