package com.movio.moviolab.controllers;

import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
@Tag(name = "Comment Controller", description = "API для управления комментариями")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Добавление нового комментария", description = "Создает новый комментарий")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий добавлен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
        @ApiResponse(responseCode = "404", description =
                "Пользователь или фильм с таким ID не найден")
    })
    @PostMapping
    public ResponseEntity<String> createComment(@RequestBody CommentDto commentDto) {
        return commentService.addComment(commentDto);
    }

    @Operation(summary = "Вывод всех комментариев", description = "Возвращает все комментарии")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарии найдены"),
        @ApiResponse(responseCode = "404", description = "Комментарии отсутствуют")
    })
    @GetMapping
    public ResponseEntity<List<CommentDto>> getAllComments() {
        List<CommentDto> comments = commentService.getAllComments().getBody();
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Получение комментариев по ID",
            description = "Возвращает комментарий по его ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий найден"),
        @ApiResponse(responseCode = "404", description = "Комментарий с таким ID не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Integer id) {
        CommentDto comment = commentService.getCommentById(id).getBody();
        return ResponseEntity.ok(comment);
    }

    @Operation(summary = "Изменение комментария", description = "Меняет содержание комментария")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий изменен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
        @ApiResponse(responseCode = "404", description = "Комментарий с таким ID не найден")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CommentDto> patchComment(@PathVariable Integer id,
                                                   @RequestBody CommentDto partialCommentDto) {
        CommentDto updatedComment = commentService.updateComment(id, partialCommentDto);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Удаление комментария", description = "Удаляет комментарий")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий удален успешно"),
        @ApiResponse(responseCode = "404", description = "Комментарий с таким ID не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Комментарий удален успешно");
    }
}
