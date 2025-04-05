package com.movio.moviolab.services;

import com.movio.moviolab.dao.CommentDao;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.exceptions.CommentException;
import com.movio.moviolab.exceptions.MovieException;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.Comment;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private static final String COMMENT_NOT_FOUND = "Комментарий не найден по id: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Фильм не найден: ";
    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден: ";
    private static final String COMMENT_NOT_BLANK_MESSAGE = "Комментарйи пуст";
    private static final String COMMENT_SIZE_MESSAGE = "Длинна комментария "
           + "должна быть от 2 до 500 символов";

    private final CommentDao commentDao;
    private final MovieDao movieDao;
    private final UserDao userDao;

    @Autowired
    public CommentService(CommentDao commentDao, MovieDao movieDao, UserDao userDao) {
        this.commentDao = commentDao;
        this.movieDao = movieDao;
        this.userDao = userDao;
    }

    public ResponseEntity<String> addComment(CommentDto commentDto) {

        String content = commentDto.getContent();

        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException(COMMENT_NOT_BLANK_MESSAGE);
        }

        if (content.length() < 2 || content.length() > 500) {
            throw new ValidationException(COMMENT_SIZE_MESSAGE);
        }

        Integer userId = commentDto.getUserId();
        Integer movieId = commentDto.getMovieId();

        if (!movieDao.existsById(movieId)) {
            throw new MovieException(MOVIE_NOT_FOUND_MESSAGE + movieId);
        }

        if (!userDao.existsById(userId)) {
            throw new UserException(USER_NOT_FOUND_MESSAGE + userId);
        }

        boolean commentExists = commentDao.existsByUserIdAndMovieIdAndContent(userId,
                movieId, commentDto.getContent());
        if (commentExists) {
            throw new CommentException("Этот пользователь уже оставил "
                    + "такой комментарий к этому фильму.");
        }

        Comment comment = convertToEntity(commentDto);
        commentDao.save(comment);

        return ResponseEntity.ok("Комментарий создан успешно");
    }

    public ResponseEntity<List<CommentDto>> getAllComments() {

        List<CommentDto> comments = commentDao.findAll().stream()
                    .map(this::convertToDto)
                    .toList();

        if (comments.isEmpty()) {
            throw new CommentException("Комментарии не найдены.");
        }

        return ResponseEntity.ok(comments);
    }


    public ResponseEntity<CommentDto> getCommentById(Integer id) {
        Comment comment = commentDao.findById(id)
                .orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND + id));
        return ResponseEntity.ok(convertToDto(comment));
    }

    public CommentDto updateComment(Integer id, CommentDto partialCommentDto) {
        Comment existingComment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND + id));

        String newContent = partialCommentDto.getContent();

        if (newContent == null || newContent.trim().isEmpty()
                || newContent.length() < 2 || newContent.length() > 500) {
            throw new ValidationException("Ваш " + COMMENT_NOT_BLANK_MESSAGE
                    + " или " + COMMENT_SIZE_MESSAGE);
        }

        existingComment.setContent(newContent);

        Comment updatedComment = commentDao.save(existingComment);

        return convertToDto(updatedComment);
    }

    public void deleteComment(Integer id) {
        Comment comment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException("Не найден комментарйи с ID: " + id));

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