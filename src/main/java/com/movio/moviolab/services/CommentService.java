package com.movio.moviolab.services;

import com.movio.moviolab.dao.CommentDao;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.exceptions.CommentNotFoundException;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.Comment;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final String COMMENT_NOT_FOUND = "Comment not found with id: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";

    private final CommentDao commentDao;
    private final MovieDao movieDao;
    private final UserDao userDao;

    @Autowired
    public CommentService(CommentDao commentDao, MovieDao movieDao,
                          UserDao userDao) {
        this.commentDao = commentDao;
        this.movieDao = movieDao;
        this.userDao = userDao;
    }

    public CommentDto addComment(CommentDto commentDto) {
        Integer userId = commentDto.getUserId();
        Integer movieId = commentDto.getMovieId();

        if (!movieDao.existsById(movieId)) {
            throw new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + movieId);
        }
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId);
        }

        Comment comment = convertToEntity(commentDto);
        return convertToDto(commentDao.save(comment));
    }

    public List<CommentDto> getAllComments() {
        return commentDao.findAll().stream().map(this::convertToDto).toList();
    }

    public CommentDto getCommentById(Integer id) {
        Comment comment = commentDao.findById(id).orElseThrow(()
                -> new CommentNotFoundException(COMMENT_NOT_FOUND + id));
        return convertToDto(comment);
    }

    public CommentDto updateComment(Integer id, CommentDto partialCommentDto) {
        Comment existingComment = commentDao.findById(id).orElseThrow(()
                -> new CommentNotFoundException(COMMENT_NOT_FOUND + id));

        if (partialCommentDto.getContent() != null) {
            existingComment.setContent(partialCommentDto.getContent());
        }

        return convertToDto(commentDao.save(existingComment));
    }

    public void deleteComment(Integer id) {
        Comment comment = commentDao.findById(id).orElseThrow(()
                -> new CommentNotFoundException(COMMENT_NOT_FOUND + id));
        commentDao.delete(comment);
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setUserId(comment.getUserId());
        commentDto.setMovieId(comment.getMovieId());
        return commentDto;
    }

    private Comment convertToEntity(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setContent(commentDto.getContent());
        comment.setUserId(commentDto.getUserId());
        comment.setMovieId(commentDto.getMovieId());
        return comment;
    }
}
