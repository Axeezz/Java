package com.movio.moviolab.exceptions;

public class UserAlreadyAssociatedException extends RuntimeException {
    public UserAlreadyAssociatedException(String message) {
        super(message);
    }
}
