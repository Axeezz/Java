package com.movio.moviolab.services;

import com.movio.moviolab.dao.CommentDao;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.exceptions.CommentException;
import com.movio.moviolab.exceptions.MovieException;
import com.movio.moviolab.exceptions.UserException;
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
        Integer userId = commentDto.getUserId();
        Integer movieId = commentDto.getMovieId();

        // Проверяем существование фильма и пользователя
        if (!movieDao.existsById(movieId)) {
            throw new MovieException(MOVIE_NOT_FOUND_MESSAGE + movieId);
        }
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
        try {
            // Получаем все комментарии из базы данных
            List<CommentDto> comments = commentDao.findAll().stream()
                    .map(this::convertToDto)
                    .toList();
            // Возвращаем список комментариев с кодом 200
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            // В случае ошибки возвращаем статус 500 и пустой список
            return ResponseEntity.status(500).body(List.of());
        }
    }


    public ResponseEntity<CommentDto> getCommentById(Integer id) {
        try {
            // Поиск комментария по ID
            Comment comment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND + id));

            // Преобразуем найденный комментарий в DTO и возвращаем
            return ResponseEntity.ok(convertToDto(comment));
        } catch (CommentException e) {
            // Пробрасываем исключение, если комментарий не найден
            // В контроллере будет обработано исключение, и будет возвращено сообщение в теле ответа
            throw e;
        } catch (Exception e) {
            // В случае других ошибок пробрасываем их дальше
            throw new CommentException("Ошибка при получении комментария: " + e.getMessage(), e);
        }
    }





    public CommentDto updateComment(Integer id, CommentDto partialCommentDto) {
        try {
            // Находим комментарий по ID. Если не найден, выбрасываем исключение
            Comment existingComment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND + id));

            // Обновляем только те поля, которые переданы в partialCommentDto
            if (partialCommentDto.getContent() != null) {
                existingComment.setContent(partialCommentDto.getContent());
            }

            // Сохраняем изменения в базе данных
            Comment updatedComment = commentDao.save(existingComment);

            // Возвращаем обновленный комментарий
            return convertToDto(updatedComment);
        } catch (CommentException e) {
            // Перехватываем исключение CommentException и пробрасываем его дальше
            throw e;  // Пробрасываем дальше, чтобы контроллер мог обработать ошибку
        } catch (Exception e) {
            // В случае непредвиденной ошибки выбрасываем исключение
            throw new CommentException("Error occurred while updating the comment", e);
        }
    }



    // Метод для удаления комментария
    public void deleteComment(Integer id) {
        try {
            // Поиск комментария по ID
            Comment comment = commentDao.findById(id)
                    .orElseThrow(() -> new CommentException("Comment not found with ID: " + id));

            // Удаление комментария
            commentDao.delete(comment);
        } catch (CommentException ex) {
            // Пробрасываем исключение, если комментарий не найден
            throw ex;  // Re-throw the exception if comment not found
        } catch (Exception e) {
            // Обработка других исключений
            throw new CommentException("Error occurred while deleting the comment", e);
        }
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
