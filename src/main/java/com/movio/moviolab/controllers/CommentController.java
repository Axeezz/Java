package com.movio.moviolab.controllers;

import com.movio.moviolab.models.Comment;
import com.movio.moviolab.services.CommentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Method to add a new comment
    @PostMapping
    public Comment addComment(@RequestBody Comment comment) {
        Integer userId = comment.getUserId(); // Получаем userId из тела запроса
        Integer movieId = comment.getMovieId(); // Получаем movieId из тела запроса
        // Логика для обработки комментария
        return commentService.addComment(userId, movieId, comment);
    }

    // Method to get all comments (can be customized as needed)
    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }

    // Method to get a specific comment by its ID
    @GetMapping("/{id}")
    public Comment getCommentById(@PathVariable Integer id) {
        return commentService.getCommentById(id);
    }

    // Method to update a specific comment (partial update)
    @PatchMapping("/{id}")
    public Comment patchComment(@PathVariable Integer id, @RequestBody Comment partialComment) {
        return commentService.updateComment(id, partialComment);
    }

    // Method to delete a specific comment
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
    }
}
