package com.movio.moviolab.testservice;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.exceptions.BadRequestException;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import com.movio.moviolab.services.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieService2Test {

    private static final String CACHE_PREFIX_MOVIE_GENRE = "movie_genre_";
    @Mock
    private MovieDao movieDao;

    @Mock
    private UserDao userDao;

    @Mock
    private InMemoryCache inMemoryCache;

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
}
