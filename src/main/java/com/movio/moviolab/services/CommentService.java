package com.movio.moviolab.services;

import com.movio.moviolab.exceptions.CommentNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.repositories.CommentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final String COMMENT_NOT_FOUND = "Comment not found with id: ";

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment addComment(Integer userId, Integer movieId, Comment comment) {
        comment.setUserId(userId);
        comment.setMovieId(movieId);
        return commentRepository.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentById(Integer id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new CommentNotFoundException(COMMENT_NOT_FOUND + id);
        }
    }

    public Comment updateComment(Integer id, Comment partialComment) {
        Optional<Comment> existingCommentOptional = commentRepository.findById(id);
        if (existingCommentOptional.isPresent()) {
            Comment existingComment = existingCommentOptional.get();
            if (partialComment.getContent() != null) {
                existingComment.setContent(partialComment.getContent());
            }
            return commentRepository.save(existingComment);
        } else {
            throw new CommentNotFoundException(COMMENT_NOT_FOUND + id);
        }
    }

    public void deleteComment(Integer id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            commentRepository.delete(comment.get());
        } else {
            throw new CommentNotFoundException(COMMENT_NOT_FOUND + id);
        }
    }
}
