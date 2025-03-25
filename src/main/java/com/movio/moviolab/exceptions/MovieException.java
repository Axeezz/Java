package com.movio.moviolab.exceptions;

import lombok.Getter;

@Getter
public class MovieException extends RuntimeException {
    private final String errorCode;

    public MovieException(String message) {
        super(message);
        this.errorCode = "400";
    }
}
