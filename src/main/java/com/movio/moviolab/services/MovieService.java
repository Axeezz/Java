package com.movio.moviolab.services;

import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import com.movio.moviolab.repositories.MovieRepository;
import com.movio.moviolab.repositories.UserRepository;
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

    private final MovieDao movieDao;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @Autowired
    public MovieService(com.movio.moviolab.dao.MovieDao movieDao,
                        MovieRepository movieRepository, UserRepository userRepository) {
        this.movieDao = movieDao;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    public List<Movie> getMovies(String genre, Integer year, String title) {
        return movieDao.findAll().stream()
                .filter(movie -> genre == null || movie.getGenre().equalsIgnoreCase(genre))
                .filter(movie -> year == null || movie.getYear().equals(year))
                .filter(movie -> title == null || movie.getTitle().equalsIgnoreCase(title))
                .toList();  // Using Stream.toList() instead of collect(Collectors.toList())
    }

    public Movie getMovieById(Integer id) {
        return movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
    }

    public Movie addMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        List<Movie> existingMovies = movieDao
                .findByGenreAndYearAndTitle(movie.getGenre(), movie.getYear(), movie.getTitle());

        if (!existingMovies.isEmpty()) {
            throw new IllegalArgumentException(MOVIE_ALREADY_EXISTS_MESSAGE
                    + movie.getTitle() + ", " + movie.getGenre() + ", " + movie.getYear());
        }
        return movieDao.save(movie);
    }

    @Transactional
    public void deleteMovieById(Integer id) {
        if (!movieDao.existsById(id)) {
            throw new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id);
        }
        movieDao.deleteById(id);
    }

    @Transactional
    public Movie updateMovie(Integer id, Movie updatedMovie) {
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));

        // Обновление полей
        movie.setTitle(updatedMovie.getTitle());
        movie.setGenre(updatedMovie.getGenre());
        movie.setYear(updatedMovie.getYear());

        // Не нужно вызывать save(), если объект уже отслеживается
        return movie;
    }


    public Movie patchMovie(Integer id, Movie partialMovie) {
        Movie movie = movieDao.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));

        if (partialMovie.getTitle() != null) {
            movie.setTitle(partialMovie.getTitle());
        }
        if (partialMovie.getGenre() != null) {
            movie.setGenre(partialMovie.getGenre());
        }
        if (partialMovie.getYear() != null) {
            movie.setYear(partialMovie.getYear());
        }

        return movieDao.save(movie);
    }

    public List<Comment> getCommentsByMovieId(Integer id) {
        Movie movie = movieDao.findById(id).orElseThrow(()
                -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
        return movie.getComments();
    }

    public ResponseEntity<Void> addUserToMovie(Integer movieId, Integer userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException(MOVIE_NOT_FOUND_MESSAGE + movieId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        movie.getUsers().add(user);
        user.getMovies().add(movie);

        movieRepository.save(movie);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<Void> removeUserFromMovie(Integer movieId, Integer userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException(MOVIE_NOT_FOUND_MESSAGE + movieId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        movie.getUsers().remove(user);
        user.getMovies().remove(movie);

        movieRepository.save(movie);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public List<User> getUsersForMovie(Integer movieId) {
        // Find the movie by its ID, or throw an exception if not found
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        // Return the set of users associated with this movie
        return movie.getUsers();  // Assuming getUsers() returns a Set<User>
    }

}
