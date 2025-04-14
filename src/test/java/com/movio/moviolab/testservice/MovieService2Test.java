package com.movio.moviolab.testservice;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.exceptions.BadRequestException;
import com.movio.moviolab.exceptions.MovieException;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import com.movio.moviolab.services.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieService2Test {

    private static final String CACHE_PREFIX_MOVIE_GENRE = "movie_genre_";
    @Mock
    private MovieDao movieDao;

    @Mock
    private UserDao userDao;

    @Mock
    private InMemoryCache inMemoryCache;

    @Mock
    private Movie movie;

    @InjectMocks
    private MovieService movieService;

    @Test
    void testAddMoviesBulk_EmptyList() {
        List<MovieDto> emptyList = emptyList();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> movieService.addMoviesBulk(emptyList));


        assertEquals("Список фильмов не может быть пуст", exception.getMessage());
    }

    @Test
    void testRemoveUserFromMovie_ClearsCacheForAllUserMovies() {
        Movie movieTest1 = new Movie();
        movieTest1.setId(1);
        movieTest1.setGenre("Action");

        Movie movieTest2 = new Movie();
        movieTest2.setId(2);
        movieTest2.setGenre("Drama");

        User user = new User();
        user.setId(1);
        user.setMovies(new HashSet<>(Arrays.asList(movieTest1, movieTest2)));

        when(movieDao.findById(1)).thenReturn(Optional.of(movieTest1));
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        movieService.removeUserFromMovie(1, 1);

        verify(inMemoryCache).remove(CACHE_PREFIX_MOVIE_GENRE + "Action");
        verify(inMemoryCache).remove(CACHE_PREFIX_MOVIE_GENRE + "Drama");
    }

    @Test
    void testAddMoviesBulk_Success() {
        MovieDto dto1 = new MovieDto();
        dto1.setId(0);
        dto1.setTitle("Movie 1");
        dto1.setGenre("Action");
        dto1.setYear(2021);

        MovieDto dto2 = new MovieDto();
        dto2.setId(0);
        dto2.setTitle("Movie 2");
        dto2.setGenre("Drama");
        dto2.setYear(2022);

        when(movieDao.findByGenreAndYearAndTitle("Action", 2021, "Movie 1")).thenReturn(emptyList());
        when(movieDao.findByGenreAndYearAndTitle("Drama", 2022, "Movie 2")).thenReturn(emptyList());

        when(movieDao.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<MovieDto> result = movieService.addMoviesBulk(List.of(dto1, dto2));

        assertEquals(2, result.size());
        assertEquals("Movie 1", result.get(0).getTitle());
        assertEquals("Movie 2", result.get(1).getTitle());

        verify(movieDao).saveAll(argThat(list ->
                list.size() == 2 &&
                        list.getFirst().getTitle().equals("Movie 1") &&
                        list.get(1).getTitle().equals("Movie 2")
        ));
    }

    @Test
    void testDeleteMovieById_NotFound() {
        when(movieDao.findById(99)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.deleteMovieById(99));
        assertEquals("Фильм не найден: 99", exception.getMessage());
    }

    @Test
    void testGetCommentsByMovieId_NotFound() {
        when(movieDao.findById(404)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.getCommentsByMovieId(404));
        assertEquals("Фильм не найден: 404", exception.getMessage());
    }

    @Test
    void testAddUserToMovie_MovieNotFound() {
        when(movieDao.findById(1)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.addUserToMovie(1, 1));
        assertEquals("Фильм не найден: 1", exception.getMessage());
    }

    @Test
    void testRemoveUserFromMovie_UserNotFound() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(userDao.findById(1)).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class, () ->
                movieService.removeUserFromMovie(1, 1)
        );

        assertEquals("Пользователь не найден:1", exception.getMessage());
    }

    @Test
    void testRemoveUserFromMovie_MovieNotFound() {
        when(movieDao.findById(1)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () ->
                movieService.removeUserFromMovie(1, 1)
        );

        assertEquals("Фильм не найден: 1", exception.getMessage());
    }

    @Test
    void testRemoveUserFromMovie_UserNotInMovie() {
        User user = new User();
        user.setId(1);

        movie.setId(1);
        movie.setUsers(Collections.emptyList());
        // пусто, пользователь не прикреплён

        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        movieService.removeUserFromMovie(1, 1);

        // проверяем, что ничего не удалилось, но и исключения не было
        assertTrue(movie.getUsers().isEmpty());
        verify(movieDao).save(movie);
    }

    @Test
    void testValidateMandatoryFields_TitleIsNull_ShouldThrow() {
        MovieDto dto = new MovieDto();
        dto.setTitle(null);
        dto.setGenre("Action");
        dto.setYear(2020);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> movieService.addMovie(dto));
        assertEquals("Название пусто или превышает 100 символов", ex.getMessage());
    }

    @Test
    void testValidateMandatoryFields_TitleTooLong_ShouldThrow() {
        MovieDto dto = new MovieDto();
        dto.setTitle("A".repeat(101));
        dto.setGenre("Action");
        dto.setYear(2020);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> movieService.addMovie(dto));
        assertEquals("Название пусто или превышает 100 символов", ex.getMessage());
    }

    @Test
    void testValidateMandatoryFields_GenreIsEmpty_ShouldThrow() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Test");
        dto.setGenre(" ");
        dto.setYear(2020);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> movieService.addMovie(dto));
        assertEquals("Жанр пуст или превышает 50 символов", ex.getMessage());
    }

    @Test
    void testValidateMandatoryFields_GenreTooLong_ShouldThrow() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Test");
        dto.setGenre("G".repeat(51));
        dto.setYear(2020);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> movieService.addMovie(dto));
        assertEquals("Жанр пуст или превышает 50 символов", ex.getMessage());
    }

    @Test
    void testValidateYear_TooOldOrZero_ShouldThrow() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Test");
        dto.setGenre("Action");
        dto.setYear(0);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> movieService.addMovie(dto));
        assertEquals("Год пуст или меньше нуля", ex.getMessage());
    }

    @Test
    void testValidateYear_InFuture_ShouldThrow() {
        int nextYear = LocalDate.now().getYear() + 1;

        MovieDto dto = new MovieDto();
        dto.setTitle("Test");
        dto.setGenre("Action");
        dto.setYear(nextYear);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> movieService.addMovie(dto));
        assertEquals("Год не может быть больше текущего", ex.getMessage());
    }

    @Test
    void testValidMovie_ShouldPass() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Valid Title");
        dto.setGenre("Drama");
        dto.setYear(LocalDate.now().getYear());

        // тут можно мокнуть movieDao.save и вернуть что-то валидное
        when(movieDao.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> movieService.addMovie(dto));
    }

    @Test
    void testConvertToDto_WithoutComments() {
        Movie movie153 = new Movie();
        movie153.setId(1);
        movie153.setTitle("Inception");
        movie153.setGenre("Sci-Fi");
        movie153.setYear(2010);
        movie153.setComments(null);

        MovieDto dto = movieService.convertToDto(movie153);

        assertEquals(1, dto.getId());
        assertEquals("Inception", dto.getTitle());
        assertEquals("Sci-Fi", dto.getGenre());
        assertEquals(2010, dto.getYear());
        assertTrue(dto.getComments().isEmpty());
    }

    @Test
    void testConvertToDto_WithComments() {
        Comment comment = new Comment();
        comment.setId(1);
        comment.setContent("Great movieTry!");

        Movie movieTry = new Movie();
        movieTry.setId(2);
        movieTry.setTitle("Matrix");
        movieTry.setGenre("Action");
        movieTry.setYear(1999);
        movieTry.setComments(List.of(comment));

        MovieDto dto = movieService.convertToDto(movieTry);

        assertNotNull(dto.getComments());
        assertEquals(1, dto.getComments().size());
        assertEquals("Great movieTry!", dto.getComments().getFirst().getContent());
    }

    @Test
    void testIsInvalidPartial_AllFieldsInvalid_ShouldReturnTrue() {
        MovieDto dto = new MovieDto();
        dto.setTitle("");
        dto.setGenre(null);
        dto.setYear(LocalDate.now().getYear() + 1);

        boolean result = movieService.isInvalidPartial(dto);
        assertTrue(result, "Ожидается true, если все поля невалидны");
    }

    @Test
    void testIsInvalidPartial_OneFieldValid_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Valid Title");
        dto.setGenre("");
        dto.setYear(0);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Ожидается false, если хотя бы одно поле валидное");
    }

    @Test
    void testIsInvalidPartial_AllFieldsValid_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Valid Title");
        dto.setGenre("Drama");
        dto.setYear(LocalDate.now().getYear());

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Ожидается false, если все поля валидные");
    }

    @Test
    void testIsInvalidPartial_EmptyTitle_ValidGenreAndYear_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("");
        dto.setGenre("Drama");
        dto.setYear(2020);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только title невалиден");
    }

    @Test
    void testIsInvalidPartial_LongTitle_ValidGenreAndYear_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("A".repeat(101));
        dto.setGenre("Action");
        dto.setYear(2020);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только title слишком длинный");
    }

    @Test
    void testIsInvalidPartial_EmptyGenre_ValidTitleAndYear_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Title");
        dto.setGenre("");
        dto.setYear(2020);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только genre невалиден");
    }

    @Test
    void testIsInvalidPartial_LongGenre_ValidTitleAndYear_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Title");
        dto.setGenre("G".repeat(51));
        dto.setYear(2020);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только genre слишком длинный");
    }

    @Test
    void testIsInvalidPartial_YearNull_ValidTitleAndGenre_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Title");
        dto.setGenre("Genre");
        dto.setYear(null);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только year = null");
    }

    @Test
    void testIsInvalidPartial_YearLessThanZero_ValidTitleAndGenre_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Title");
        dto.setGenre("Genre");
        dto.setYear(-5);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только year < 0");
    }

    @Test
    void testIsInvalidPartial_YearInFuture_ValidTitleAndGenre_ShouldReturnFalse() {
        MovieDto dto = new MovieDto();
        dto.setTitle("Title");
        dto.setGenre("Genre");
        dto.setYear(LocalDate.now().getYear() + 1);

        boolean result = movieService.isInvalidPartial(dto);
        assertFalse(result, "Должно быть false, так как только year в будущем");
    }

}
