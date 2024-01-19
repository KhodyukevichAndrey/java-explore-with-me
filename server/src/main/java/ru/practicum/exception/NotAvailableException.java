package ru.practicum.exception;

public class NotAvailableException extends RuntimeException {
    public NotAvailableException(final String message) {
        super(message);
    }
}
