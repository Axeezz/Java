package com.movio.moviolab.testservice;

import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.MovieException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.services.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieDao movieDao;

    @Mock
    private UserDao userDao;

    @Mock
    private InMemoryCache inMemoryCache;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private MovieDto movieDto;
    private User user, user1, user2;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1);
        movie.setTitle("Test Movie");
        movie.setGenre("Action");
        movie.setYear(2023);

        movieDto = new MovieDto();
        movieDto.setId(1);
        movieDto.setTitle("Test Movie");
        movieDto.setGenre("Action");
        movieDto.setYear(2023);

        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        user1 = new User();
        user1.setId(1);
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");

        user2 = new User();
        user2.setId(2);
        user2.setName("Jane Doe");
        user2.setEmail("jane.doe@example.com");
            }

    @Test
    void testGetMovies_FiltersCorrectly() {
        Movie movie2 = new Movie();
        movie2.setId(2);
        movie2.setTitle("Another Movie");
        movie2.setGenre("Comedy");
        movie2.setYear(2022);

        List<Movie> movieList = List.of(movie, movie2);

        when(movieDao.findAll()).thenReturn(movieList);

        List<MovieDto> result = movieService.getMovies("Action", null, null);

        assertEquals(1, result.size());
        assertEquals("Test Movie", result.getFirst().getTitle());
    }

    @Test
    void testGetMovies_FilterNoMovies() {
        when(movieDao.findAll()).thenReturn(emptyList());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.getMovies("genre", 2029, "Title"));

        assertEquals("Фильм не найден: фильм с такими параметрами не существует", exception.getMessage());
    }

    @Test
    void testGetMovieById_MovieFound() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));

        MovieDto result = movieService.getMovieById(1);

        assertNotNull(result);
        assertEquals(movie.getTitle(), result.getTitle());
    }

    @Test
    void testGetMovieById_MovieNotFound() {
        when(movieDao.findById(99)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.getMovieById(99));
        assertEquals("Фильм не найден: 99", exception.getMessage());
    }

    @Test
    void testAddMovie_Success() {
        when(movieDao.findByGenreAndYearAndTitle(movie.getGenre(), movie.getYear(), movie.getTitle()))
                .thenReturn(List.of());

        when(movieDao.save(any(Movie.class))).thenReturn(movie);

        MovieDto result = movieService.addMovie(movieDto);

        assertNotNull(result);
        assertEquals(movie.getTitle(), result.getTitle());
    }

    @Test
    void testAddMovie_MovieAlreadyExists() {
        when(movieDao.findByGenreAndYearAndTitle(movie.getGenre(), movie.getYear(), movie.getTitle()))
                .thenReturn(List.of(movie));

        MovieException exception = assertThrows(MovieException.class, () -> movieService.addMovie(movieDto));

        assertEquals("Фильм с таким названием, жанром и годом уже существует: Test Movie, Action, 2023", exception.getMessage());
    }

    @Test
    void testUpdateMovie_Success() {
        MovieDto updatedMovieDto = new MovieDto();
        updatedMovieDto.setTitle("Updated Movie");
        updatedMovieDto.setGenre("Comedy");
        updatedMovieDto.setYear(2024);

        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(movieDao.save(any(Movie.class))).thenReturn(movie);
        doNothing().when(inMemoryCache).remove(anyString());

        MovieDto result = movieService.updateMovie(1, updatedMovieDto);

        assertNotNull(result);
        assertEquals(updatedMovieDto.getTitle(), result.getTitle());
        assertEquals(updatedMovieDto.getGenre(), result.getGenre());
    }

    @Test
    void testDeleteMovieById_Success() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        doNothing().when(movieDao).deleteById(1);
        doNothing().when(inMemoryCache).removeAll();

        movieService.deleteMovieById(1);

        verify(movieDao, times(1)).deleteById(1);
        verify(inMemoryCache, times(1)).removeAll();
    }

    @Test
    void testGetCommentsByMovieId_Success() {
        Comment comment = new Comment();
        comment.setContent("Great movie!");
        comment.setUserId(1);
        comment.setMovieId(1);

        List<Comment> comments = List.of(comment);
        movie.setComments(comments);

        when(movieDao.findById(1)).thenReturn(Optional.of(movie));

        List<CommentDto> result = movieService.getCommentsByMovieId(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment.getContent(), result.getFirst().getContent());
    }

    @Test
    void testAddUserToMovie_Success() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        doNothing().when(inMemoryCache).remove(anyString());

        ResponseEntity<String> result = movieService.addUserToMovie(1, 1);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertTrue(movie.getUsers().contains(user));
    }

    @Test
    void testRemoveUserFromMovie_Success() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(userDao.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<String> result = movieService.removeUserFromMovie(1, 1);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertFalse(movie.getUsers().contains(user));
    }

    @Test
    void testPatchMovie_Success_UpdateTitle() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(movieDao.save(any(Movie.class))).thenReturn(movie);
        doNothing().when(inMemoryCache).remove(anyString());

        MovieDto partialUpdateDto = new MovieDto();
        partialUpdateDto.setTitle("Updated Movie");

        MovieDto result = movieService.patchMovie(1, partialUpdateDto);

        assertNotNull(result);
        assertEquals("Updated Movie", result.getTitle());
        assertEquals("Action", result.getGenre());
        assertEquals(2023, result.getYear());

        verify(inMemoryCache, times(2)).remove("movie_genre_Action");
    }

    @Test
    void testPatchMovie_Success_UpdateGenre() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(movieDao.save(any(Movie.class))).thenReturn(movie);
        doNothing().when(inMemoryCache).remove(anyString());

        MovieDto partialUpdateDto = new MovieDto();
        partialUpdateDto.setGenre("Comedy");

        MovieDto result = movieService.patchMovie(1, partialUpdateDto);

        assertNotNull(result);
        assertEquals("Test Movie", result.getTitle());
        assertEquals("Comedy", result.getGenre());
        assertEquals(2023, result.getYear());

        verify(inMemoryCache, times(1)).remove("movie_genre_Action");
        verify(inMemoryCache, times(1)).remove("movie_genre_Comedy");
    }

    @Test
    void testPatchMovie_Success_UpdateYear() {
        when(movieDao.findById(1)).thenReturn(Optional.of(movie));
        when(movieDao.save(any(Movie.class))).thenReturn(movie);
        doNothing().when(inMemoryCache).remove(anyString());

        MovieDto partialUpdateDto = new MovieDto();
        partialUpdateDto.setYear(2024);

        MovieDto result = movieService.patchMovie(1, partialUpdateDto);

        assertNotNull(result);
        assertEquals("Test Movie", result.getTitle());
        assertEquals("Action", result.getGenre());
        assertEquals(2024, result.getYear());

        verify(inMemoryCache, times(2)).remove("movie_genre_Action");
    }

    @Test
    void testPatchMovie_FilmNotFound() {
        when(movieDao.findById(999)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.patchMovie(999, movieDto));

        assertEquals("Фильм не найден: 999", exception.getMessage());
    }

    @Test
    void testPatchMovie_PartialUpdateInvalid() {

        MovieDto partialUpdateDto = new MovieDto();

        ValidationException exception = assertThrows(ValidationException.class, () -> movieService.patchMovie(1, partialUpdateDto));

        assertEquals("Новый фильм *invalid*", exception.getMessage());
    }

    @Test
    void testGetUsersForMovie_FilmFound() {
        movie.setUsers(List.of(user1, user2));

        when(movieDao.findById(1)).thenReturn(Optional.of(movie));

        List<UserDto> userDtos = movieService.getUsersForMovie(1);

        assertEquals(2, userDtos.size());

        assertEquals(user1.getId(), userDtos.getFirst().getId());
        assertEquals(user1.getName(), userDtos.getFirst().getName());
        assertEquals(user1.getEmail(), userDtos.getFirst().getEmail());

        assertEquals(user2.getId(), userDtos.get(1).getId());
        assertEquals(user2.getName(), userDtos.get(1).getName());
        assertEquals(user2.getEmail(), userDtos.get(1).getEmail());

        verify(movieDao, times(1)).findById(1);
    }

    @Test
    void testGetUsersForMovie_FilmNotFound() {
        when(movieDao.findById(1)).thenReturn(Optional.empty());

        MovieException exception = assertThrows(MovieException.class, () -> movieService.getUsersForMovie(1));

        assertEquals("Фильм не найден по id: 1", exception.getMessage());
    }
}