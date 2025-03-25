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

    private static final String COMMENT_NOT_FOUND = "Comment not found with id: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";
    private static final String COMMENT_NOT_BLANK_MESSAGE = "Comment is mandatory";
    private static final String COMMENT_SIZE_MESSAGE = "Comment must "
           + "be between 2 and 500 characters";


    private final CommentDao commentDao;
    private final MovieDao movieDao;
    private final UserDao userDao;

    @Autowired
    public CommentService(CommentDao commentDao, MovieDao movieDao, UserDao userDao) {
        this.commentDao = commentDao;
        this.movieDao = movieDao;
        this.userDao = userDao;
    }

    // Метод для добавления комментария
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

        // Проверка существования фильма
        if (!movieDao.existsById(movieId)) {
            throw new MovieException(MOVIE_NOT_FOUND_MESSAGE + movieId);
        }

        // Проверка существования пользователя
        if (!userDao.existsById(userId)) {
            throw new UserException(USER_NOT_FOUND_MESSAGE + userId);
        }

        boolean commentExists = commentDao.existsByUserIdAndMovieIdAndContent(userId,
                movieId, commentDto.getContent());
        if (commentExists) {
            throw new CommentException("A comment with the same content already"
                    + "exists for this user and movie.");
        }

        // Создаём и сохраняем комментарий
        Comment comment = convertToEntity(commentDto);
        commentDao.save(comment);

        // Возвращаем успешный ответ
        return ResponseEntity.ok("Comment created successfully");
    }

    public ResponseEntity<List<CommentDto>> getAllComments() {

        List<CommentDto> comments = commentDao.findAll().stream()
                    .map(this::convertToDto)
                    .toList();

        if (comments.isEmpty()) {
            throw new CommentException("No comments found.");
        }

        return ResponseEntity.ok(comments);
    }


    public ResponseEntity<CommentDto> getCommentById(Integer id) {
        // Поиск комментария по ID
        Comment comment = commentDao.findById(id)
                .orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND + id));
        // Преобразуем найденный комментарий в DTO и возвращаем
        return ResponseEntity.ok(convertToDto(comment));
    }

    public CommentDto updateComment(Integer id, CommentDto partialCommentDto) {
        // Находим комментарий по ID. Если не найден, выбрасываем исключение
        Comment existingComment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND + id));

        String newContent = partialCommentDto.getContent();

        if (newContent == null || newContent.trim().isEmpty()
                || newContent.length() < 2 || newContent.length() > 500) {
            throw new ValidationException("Your " + COMMENT_NOT_BLANK_MESSAGE
                    + " or " + COMMENT_SIZE_MESSAGE);
        }

        existingComment.setContent(newContent);

        // Сохраняем изменения в базе данных
        Comment updatedComment = commentDao.save(existingComment);

        // Возвращаем обновленный комментарий
        return convertToDto(updatedComment);
    }



    // Метод для удаления комментария
    public void deleteComment(Integer id) {
        // Поиск комментария по ID
        Comment comment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException("Comment not found with ID: " + id));

        // Удаление комментария
        commentDao.delete(comment);
    }

    // Преобразование модели Comment в DTO
    private CommentDto convertToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setUserId(comment.getUserId());
        commentDto.setMovieId(comment.getMovieId());
        return commentDto;
    }

    // Преобразование DTO в модель Comment
    private Comment convertToEntity(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setContent(commentDto.getContent());
        comment.setUserId(commentDto.getUserId());
        comment.setMovieId(commentDto.getMovieId());
        return comment;
    }
}
