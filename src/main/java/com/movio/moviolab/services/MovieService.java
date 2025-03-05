package com.movio.moviolab.services;

import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.repositories.MovieRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found: ";

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getMovies(String genre, Integer year) {
        if (genre != null && year != null) {
            return movieRepository.findByGenreIgnoreCaseAndYear(genre, year);
        } else if (genre != null) {
            return movieRepository.findByGenreIgnoreCase(genre);
        } else if (year != null) {
            return movieRepository.findByYear(year);
        } else {
            return movieRepository.findAll();
        }
    }

    public Movie getMovieByTitle(String title) {
        return movieRepository.findByTitleIgnoreCase(title)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + title));
    }

    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
    }

    public Movie addMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        return movieRepository.save(movie);
    }

    public void deleteMovieById(Integer id) {
        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id);
        }
        movieRepository.deleteById(id);
    }

    public Movie updateMovie(Integer id, Movie updatedMovie) {
        return movieRepository.findById(id).map(movie -> {
            movie.setTitle(updatedMovie.getTitle());
            movie.setGenre(updatedMovie.getGenre());
            movie.setYear(updatedMovie.getYear());
            return movieRepository.save(movie);
        }).orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND_MESSAGE + id));
    }

    public Movie patchMovie(Integer id, Movie partialMovie) {
        Movie movie = movieRepository.findById(id)
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

        return movieRepository.save(movie);
    }

}