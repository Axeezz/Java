package com.movio.moviolab.services;

import com.movio.moviolab.dao.CommentDao;
import com.movio.moviolab.exceptions.CommentNotFoundException;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.repositories.MovieRepository;
import com.movio.moviolab.repositories.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final String COMMENT_NOT_FOUND = "Comment not found with id: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";

    private final CommentDao commentDao;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentDao commentDao, MovieRepository movieRepository,
                          UserRepository userRepository) {
        this.commentDao = commentDao;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Comment comment) {
        Integer userId = comment.getUserId();
        Integer movieId = comment.getMovieId();

        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + movieId);
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId);
        }

        return commentDao.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentDao.findAll();
    }

    public Comment getCommentById(Integer id) {
        return commentDao.findById(id).orElseThrow(()
                -> new CommentNotFoundException(COMMENT_NOT_FOUND + id));
    }

    public Comment updateComment(Integer id, Comment partialComment) {
        Comment existingComment = commentDao.findById(id).orElseThrow(()
                -> new CommentNotFoundException(COMMENT_NOT_FOUND + id));

        if (partialComment.getContent() != null) {
            existingComment.setContent(partialComment.getContent());
        }

        return commentDao.save(existingComment);
    }

    public void deleteComment(Integer id) {
        Comment comment = commentDao.findById(id).orElseThrow(()
                -> new CommentNotFoundException(COMMENT_NOT_FOUND + id));
        commentDao.delete(comment);
    }
}
