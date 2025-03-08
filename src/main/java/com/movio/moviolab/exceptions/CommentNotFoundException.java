package com.movio.moviolab.exceptions;

public class CommentNotFoundException extends RuntimeException {

    // Constructor with custom message
    public CommentNotFoundException(String message) {
        super(message);
    }
}
