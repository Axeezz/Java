package com.movio.moviolab.services;

import com.movio.moviolab.exceptions.CommentNotFoundException;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.repositories.CommentRepository;
import com.movio.moviolab.repositories.MovieRepository;
import com.movio.moviolab.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final String COMMENT_NOT_FOUND = "Comment not found with id: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";

    private final CommentRepository commentRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          MovieRepository movieRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Integer userId, Integer movieId, Comment comment) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + movieId);
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId);
        }

        comment.setMovieId(movieId);
        comment.setUserId(userId);

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
