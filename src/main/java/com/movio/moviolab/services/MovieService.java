package com.movio.moviolab.services;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private static final String MOVIE_ALREADY_EXISTS_MESSAGE =
            "Movie with the same title, genre, and year already exists: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";
    private static final String CACHE_PREFIX_MOVIE_GENRE = "movie_genre_";

    private final MovieDao movieDao;
    private final UserDao userDao;
    private final InMemoryCache inMemoryCache;

    @Autowired
    public MovieService(MovieDao movieDao, UserDao userDao, InMemoryCache inMemoryCache) {
        this.movieDao = movieDao;
        this.userDao = userDao;
        this.inMemoryCache = inMemoryCache;
    }

    public List<MovieDto> getMovies(String genre, Integer year, String title) {
        return movieDao.findAll().stream()
                .filter(movie -> genre == null || movie.getGenre().equalsIgnoreCase(genre))
                .filter(movie -> year == null || movie.getYear().equals(year))
                .filter(movie -> title == null || movie.getTitle().equalsIgnoreCase(title))
                .map(this::convertToDto)
                .toList();
    }

    public MovieDto getMovieById(Integer id) {
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
        return convertToDto(movie);
    }

    public MovieDto addMovie(MovieDto movieDto) {
        Movie movie = convertToEntity(movieDto);

        List<Movie> existingMovies = movieDao
                .findByGenreAndYearAndTitle(movie.getGenre(), movie.getYear(), movie.getTitle());

        if (!existingMovies.isEmpty()) {
            throw new IllegalArgumentException(MOVIE_ALREADY_EXISTS_MESSAGE
                    + movie.getTitle() + ", " + movie.getGenre() + ", " + movie.getYear());
        }

        Movie savedMovie = movieDao.save(movie);

        return convertToDto(savedMovie);
    }

    @Transactional
    public void deleteMovieById(Integer id) {
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));

        movieDao.deleteById(id);

        inMemoryCache.remove(CACHE_PREFIX_MOVIE_GENRE + movie.getGenre());
    }

    @Transactional
    public MovieDto updateMovie(Integer id, MovieDto updatedMovieDto) {
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));

        final String oldGenre = movie.getGenre();

        movie.setTitle(updatedMovieDto.getTitle());
        movie.setGenre(updatedMovieDto.getGenre());
        movie.setYear(updatedMovieDto.getYear());

        Movie updatedMovie = movieDao.save(movie);

        inMemoryCache.remove(CACHE_PREFIX_MOVIE_GENRE + oldGenre);
        inMemoryCache.remove(CACHE_PREFIX_MOVIE_GENRE + updatedMovie.getGenre());

        return convertToDto(updatedMovie);
    }

    @Transactional
    public MovieDto patchMovie(Integer id, MovieDto partialMovieDto) {
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));

        final String oldGenre = movie.getGenre();

        if (partialMovieDto.getTitle() != null) {
            movie.setTitle(partialMovieDto.getTitle());
        }
        if (partialMovieDto.getGenre() != null) {
            movie.setGenre(partialMovieDto.getGenre());
        }
        if (partialMovieDto.getYear() != null) {
            movie.setYear(partialMovieDto.getYear());
        }

        Movie updatedMovie = movieDao.save(movie);

        inMemoryCache.remove(CACHE_PREFIX_MOVIE_GENRE + oldGenre);
        inMemoryCache.remove(CACHE_PREFIX_MOVIE_GENRE + updatedMovie.getGenre());

        return convertToDto(updatedMovie);
    }

    public List<CommentDto> getCommentsByMovieId(Integer id) {
        Movie movie = movieDao.findById(id).orElseThrow(()
                -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
        return movie.getComments().stream().map(this::convertToDto).toList();
    }

    @Transactional
    public ResponseEntity<String> addUserToMovie(Integer movieId, Integer userId) {
        Movie movie = movieDao.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + movieId));
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (movie.getUsers().contains(user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("User is already associated with this movie.");
        }

        movie.getUsers().add(user);
        user.getMovies().add(movie);

        movieDao.save(movie);
        userDao.save(user);

        for (Movie userMovies : user.getMovies()) {
            String genre = userMovies.getGenre();

            String key = CACHE_PREFIX_MOVIE_GENRE + genre;
            inMemoryCache.remove(key);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Transactional
    public ResponseEntity<Void> removeUserFromMovie(Integer movieId, Integer userId) {
        Movie movie = movieDao.findById(movieId)
                .orElseThrow(() -> new RuntimeException(MOVIE_NOT_FOUND_MESSAGE + movieId));
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        movie.getUsers().remove(user);
        user.getMovies().remove(movie);

        movieDao.save(movie);
        userDao.save(user);

        for (Movie userMovies : user.getMovies()) {
            String genre = userMovies.getGenre();

            String key = CACHE_PREFIX_MOVIE_GENRE + genre;
            inMemoryCache.remove(key);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    public List<UserDto> getUsersForMovie(Integer movieId) {
        Movie movie = movieDao.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        return movie.getUsers().stream().map(this::convertToDto).toList();
    }

    private MovieDto convertToDto(Movie movie) {
        MovieDto movieDto = new MovieDto();
        movieDto.setId(movie.getId());
        movieDto.setTitle(movie.getTitle());
        movieDto.setGenre(movie.getGenre());
        movieDto.setYear(movie.getYear());

        if (movie.getComments() != null) {
            List<CommentDto> commentDtos = movie.getComments().stream()
                    .map(this::convertToDto)
                    .toList();
            movieDto.setComments(commentDtos);
        }

        if (movie.getUsers() != null) {
            List<UserDto> userDtos = movie.getUsers().stream()
                    .map(this::convertToDto)
                    .toList();
            movieDto.setUsers(userDtos);
        }

        return movieDto;
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setUserId(comment.getUserId());
        commentDto.setMovieId(comment.getMovieId());
        return commentDto;
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private Movie convertToEntity(MovieDto movieDto) {
        Movie movie = new Movie();
        movie.setId(movieDto.getId());
        movie.setTitle(movieDto.getTitle());
        movie.setGenre(movieDto.getGenre());
        movie.setYear(movieDto.getYear());
        return movie;
    }
}
