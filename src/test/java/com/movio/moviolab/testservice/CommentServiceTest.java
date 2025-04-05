package com.movio.moviolab.testservice;

import com.movio.moviolab.dao.CommentDao;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.exceptions.CommentException;
import com.movio.moviolab.exceptions.MovieException;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentDao commentDao;

    @Mock
    private MovieDao movieDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private CommentService commentService;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setId(1);
        comment.setContent("Great movie!");
        comment.setUserId(1);
        comment.setMovieId(1);
    }

    @Test
    void testAddComment_Success() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("Great movie!");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        when(movieDao.existsById(1)).thenReturn(true);
        when(userDao.existsById(1)).thenReturn(true);
        when(commentDao.existsByUserIdAndMovieIdAndContent(1, 1, "Great movie!")).thenReturn(false);
        when(commentDao.save(any())).thenReturn(comment);

        ResponseEntity<String> response = commentService.addComment(commentDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Комментарий создан успешно", response.getBody());
    }

    @Test
    void testAddComment_BlankContent() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent(" ");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        ValidationException exception = assertThrows(ValidationException.class, () -> commentService.addComment(commentDto));

        assertEquals("Комментарйи пуст", exception.getMessage());
    }

    @Test
    void testAddComment_ContentTooShort() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("A");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        ValidationException exception = assertThrows(ValidationException.class, () -> commentService.addComment(commentDto));

        assertEquals("Длинна комментария должна быть от 2 до 500 символов", exception.getMessage());
    }

    @Test
    void testAddComment_ContentTooLong() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("A".repeat(501));
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        ValidationException exception = assertThrows(ValidationException.class, () -> commentService.addComment(commentDto));

        assertEquals("Длинна комментария должна быть от 2 до 500 символов", exception.getMessage());
    }

    @Test
    void testAddComment_MovieNotFound() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("Great movie!");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        when(movieDao.existsById(1)).thenReturn(false);

        MovieException exception = assertThrows(MovieException.class, () -> commentService.addComment(commentDto));

        assertEquals("Фильм не найден: 1", exception.getMessage());
    }

    @Test
    void testAddComment_UserNotFound() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("Great movie!");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        when(movieDao.existsById(1)).thenReturn(true);
        when(userDao.existsById(1)).thenReturn(false);

        UserException exception = assertThrows(UserException.class, () -> commentService.addComment(commentDto));

        assertEquals("Пользователь не найден: 1", exception.getMessage());
    }

    @Test
    void testAddComment_DuplicateComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("Great movie!");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        when(movieDao.existsById(1)).thenReturn(true);
        when(userDao.existsById(1)).thenReturn(true);
        when(commentDao.existsByUserIdAndMovieIdAndContent(1, 1, "Great movie!")).thenReturn(true);

        CommentException exception = assertThrows(CommentException.class, () -> commentService.addComment(commentDto));

        assertEquals("Этот пользователь уже оставил такой комментарий к этому фильму.", exception.getMessage());
    }

    @Test
    void testGetAllComments_Success() {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1);
        commentDto.setContent("Great movie!");
        commentDto.setUserId(1);
        commentDto.setMovieId(1);

        when(commentDao.findAll()).thenReturn(List.of(comment));

        ResponseEntity<List<CommentDto>> response = commentService.getAllComments();

        assertNotNull(response.getBody());
        assertEquals(1, commentDto.getId());
        assertEquals(1, response.getBody().size());
        assertEquals("Great movie!", response.getBody().getFirst().getContent());
    }

    @Test
    void testGetAllComments_NoComments() {
        when(commentDao.findAll()).thenReturn(List.of());

        CommentException exception = assertThrows(CommentException.class, () -> commentService.getAllComments());

        assertEquals("Комментарии не найдены.", exception.getMessage());
    }

    @Test
    void testGetCommentById_Success() {
        when(commentDao.findById(1)).thenReturn(Optional.of(comment));

        ResponseEntity<CommentDto> response = commentService.getCommentById(1);

        assertNotNull(response.getBody());
        assertEquals("Great movie!", response.getBody().getContent());
    }

    @Test
    void testGetCommentById_NotFound() {
        when(commentDao.findById(1)).thenReturn(Optional.empty());

        CommentException exception = assertThrows(CommentException.class, () -> commentService.getCommentById(1));

        assertEquals("Комментарий не найден по id: 1", exception.getMessage());
    }

    @Test
    void testUpdateComment_Success() {
        CommentDto updateDto = new CommentDto();
        updateDto.setContent("Updated comment!");

        when(commentDao.findById(1)).thenReturn(Optional.of(comment));
        when(commentDao.save(any())).thenReturn(comment);

        CommentDto updatedComment = commentService.updateComment(1, updateDto);

        assertNotNull(updatedComment);
        assertEquals("Updated comment!", updatedComment.getContent());
    }

    @Test
    void testUpdateComment_CommentInvalidSize() {
        CommentDto updateDto = new CommentDto();
        updateDto.setContent("");

        when(commentDao.findById(1)).thenReturn(Optional.of(comment));

        ValidationException exception = assertThrows(ValidationException.class, () -> commentService.updateComment(1, updateDto));

        assertEquals("Ваш Комментарйи пуст или Длинна комментария должна быть от 2 до 500 символов", exception.getMessage());
    }

    @Test
    void testUpdateComment_NullContent() {
        CommentDto updateDto = new CommentDto();
        updateDto.setContent(null);

        when(commentDao.findById(1)).thenReturn(Optional.of(comment));

        ValidationException exception = assertThrows(ValidationException.class, () -> commentService.updateComment(1, updateDto));

        assertEquals("Ваш Комментарйи пуст или Длинна комментария должна быть от 2 до 500 символов", exception.getMessage());
    }

    @Test
    void testDeleteComment_Success() {
        when(commentDao.findById(1)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1);

        verify(commentDao, times(1)).delete(comment);
    }

    @Test
    void testDeleteComment_NotFound() {
        when(commentDao.findById(1)).thenReturn(Optional.empty());

        CommentException exception = assertThrows(CommentException.class, () -> commentService.deleteComment(1));

        assertEquals("Не найден комментарйи с ID: 1", exception.getMessage());
    }
}
