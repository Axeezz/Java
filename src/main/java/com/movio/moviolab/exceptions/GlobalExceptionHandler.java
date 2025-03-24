package com.movio.moviolab.exceptions;

import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработка MovieException
    @ExceptionHandler(MovieException.class)
    public ResponseEntity<MovieDto> handleMovieException(MovieException e) {
        MovieDto errorDto = new MovieDto();
        errorDto.setTitle(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto); // 404
    }

    // Обработка CommentException
    @ExceptionHandler(CommentException.class)
    public ResponseEntity<CommentDto> handleCommentException(CommentException e) {
        CommentDto errorDto = new CommentDto();
        errorDto.setContent(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto); // 404
    }

    // Обработка UserException
    @ExceptionHandler(UserException.class)
    public ResponseEntity<UserDto> handleUserException(UserException e) {
        UserDto errorDto = new UserDto();
        errorDto.setName(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto); // 404 User Not Found
    }

    // Обработка IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>>
        handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Обработка ошибок для валидации
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
        handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // Обработка общего исключения
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return buildErrorResponse("Internal server error: "
                + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
    }

    // Унифицированный метод для создания ответа об ошибке
    private ResponseEntity<Map<String, Object>>
        buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}
