package com.movio.moviolab.services;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.MovieException;
import com.movio.moviolab.exceptions.UserAlreadyAssociatedException;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + id));
        return convertToDto(movie);
    }


    public MovieDto addMovie(MovieDto movieDto) {

        validateMovieDto(movieDto, false);

        Movie movie = convertToEntity(movieDto);

        List<Movie> existingMovies = movieDao
                .findByGenreAndYearAndTitle(movie.getGenre(), movie.getYear(), movie.getTitle());

        if (!existingMovies.isEmpty()) {
            throw new MovieException(MOVIE_ALREADY_EXISTS_MESSAGE
                    + movie.getTitle() + ", " + movie.getGenre() + ", " + movie.getYear());
        }

        Movie savedMovie = movieDao.save(movie);

        return convertToDto(savedMovie);
    }

    @Transactional
    public void deleteMovieById(Integer id) {
        movieDao.findById(id)
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + id));

        inMemoryCache.removeAll();

        movieDao.deleteById(id);
    }

    @Transactional
    public MovieDto updateMovie(Integer id, MovieDto updatedMovieDto) {

        validateMovieDto(updatedMovieDto, false);

        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + id));

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

        validateMovieDto(partialMovieDto, true);

        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + id));

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
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + id));
        return movie.getComments().stream().map(this::convertToDto).toList();
    }

    @Transactional
    public ResponseEntity<String> addUserToMovie(Integer movieId, Integer userId) {
        Movie movie = movieDao.findById(movieId)
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + movieId));
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserException("User not found: " + userId));

        if (movie.getUsers().contains(user)) {
            throw new UserAlreadyAssociatedException("User is already associated with this movie.");
        }


        movie.getUsers().add(user);
        user.getMovies().add(movie);

        movieDao.save(movie);
        userDao.save(user);

        // Clear cache for the genres of this user's movies
        for (Movie userMovies : user.getMovies()) {
            String genre = userMovies.getGenre();
            String key = CACHE_PREFIX_MOVIE_GENRE + genre;
            inMemoryCache.remove(key);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Transactional
    public ResponseEntity<String> removeUserFromMovie(Integer movieId, Integer userId) {
        Movie movie = movieDao.findById(movieId)
                .orElseThrow(() -> new MovieException(MOVIE_NOT_FOUND_MESSAGE + movieId));
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserException("User not found" + userId));

        // Remove user from movie and vice versa
        for (Movie userMovies : user.getMovies()) {
            String genre = userMovies.getGenre();
            String key = CACHE_PREFIX_MOVIE_GENRE + genre;
            inMemoryCache.remove(key);
        }

        movie.getUsers().remove(user);
        user.getMovies().remove(movie);

        movieDao.save(movie);
        userDao.save(user);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public List<UserDto> getUsersForMovie(Integer movieId) {
        Movie movie = movieDao.findById(movieId)
                .orElseThrow(() -> new MovieException("Movie not found with id: " + movieId));

        return movie.getUsers().stream().map(this::convertToDto).toList();
    }

    private void validateMovieDto(MovieDto movieDto, boolean isPartial) {

        // Validate the full set of required fields if not partial
        if (!isPartial) {
            validateMandatoryFields(movieDto);
            validateYear(movieDto.getYear());
        } else {
            // For partial validation, check only the necessary fields
            if (isInvalidPartial(movieDto)) {
                throw new ValidationException("New comment is invalid");
            }
        }
    }

    // Helper method to validate mandatory fields
    private void validateMandatoryFields(MovieDto movieDto) {
        if (isNullOrEmpty(movieDto.getTitle()) || movieDto.getTitle().length() > 100) {
            throw new ValidationException("Title is mandatory or longer then 100 characters");
        }
        if (isNullOrEmpty(movieDto.getGenre()) || movieDto.getGenre().length() > 50) {
            throw new ValidationException("Genre is mandatory or longer then 50 characters");
        }
    }

    // Helper method to validate year
    private void validateYear(Integer year) {
        if (year == null || year <= 0) {
            throw new ValidationException("Year is mandatory and must be a positive number");
        }
        if (year > LocalDate.now().getYear()) {
            throw new ValidationException("Year cannot be greater than the current year");
        }
    }

    // Helper method to check for null or empty string
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Helper method to check if partial data is invalid
    private boolean isInvalidPartial(MovieDto movieDto) {
        return (isNullOrEmpty(movieDto.getTitle()) || movieDto.getTitle().length() > 100)
                && (isNullOrEmpty(movieDto.getGenre()) || movieDto.getGenre().length() > 50)
                && (movieDto.getYear() == null || movieDto.getYear() <= 0
                || movieDto.getYear() > LocalDate.now().getYear());
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
