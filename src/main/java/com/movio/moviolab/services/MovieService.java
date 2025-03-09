package com.movio.moviolab.services;

import com.movio.moviolab.dao.CommentDao;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MovieService {

    private static final String MOVIE_ALREADY_EXISTS_MESSAGE =
            "Movie with the same title, genre, and year already exists: ";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";

    private final MovieDao movieDao;

    @Autowired
    public MovieService(com.movio.moviolab.dao.MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    public List<Movie> getMovies(String genre, Integer year, String title) {
        if (genre != null && year != null && title != null) {
            return movieDao.findByGenreAndYearAndTitle(genre, year, title);
        } else if (genre != null && title != null) {
            return movieDao.findByGenreAndTitle(genre, title);
        } else if (year != null && title != null) {
            return movieDao.findByYearAndTitle(year, title);
        } else if (genre != null) {
            return movieDao.findByGenre(genre);
        } else if (year != null) {
            return movieDao.findByYear(year);
        } else if (title != null) {
            return movieDao.findByTitle(title);
        } else {
            return movieDao.findAll();
        }
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
        return movieDao.findById(id).map(movie -> {
            movie.setTitle(updatedMovie.getTitle());
            movie.setGenre(updatedMovie.getGenre());
            movie.setYear(updatedMovie.getYear());
            return movieDao.save(movie);
        }).orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
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
}
