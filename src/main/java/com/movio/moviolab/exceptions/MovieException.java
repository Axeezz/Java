package com.movio.moviolab.exceptions;

import lombok.Getter;

// Исключение для фильма
@Getter
public class MovieException extends RuntimeException {
    private final String errorCode;

    // Конструктор с сообщением (код ошибки по умолчанию)
    public MovieException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR"; // Код ошибки по умолчанию
    }
}
