package com.movio.moviolab.controllers;

import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.services.CommentService;
import jakarta.validation.Valid;
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
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<String> createComment(@Valid @RequestBody CommentDto commentDto) {
        // Пытаемся создать комментарий
        return commentService.addComment(commentDto);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getAllComments() {
        List<CommentDto> comments = commentService.getAllComments().getBody();
        return ResponseEntity.ok(comments); // Возвращаем комментарии
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable Integer id) {
        // Получаем комментарий по ID через сервис
        CommentDto comment = commentService.getCommentById(id).getBody();
        return ResponseEntity.ok(comment); // Возвращаем найденный комментарий
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentDto> patchComment(@PathVariable Integer id,
                                                   @RequestBody CommentDto partialCommentDto) {
        CommentDto updatedComment = commentService.updateComment(id, partialCommentDto);
        return ResponseEntity.ok(updatedComment); // Возвращаем обновленный комментарий
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
