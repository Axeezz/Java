package com.movio.moviolab.dao;

import com.movio.moviolab.models.Movie;
import com.movio.moviolab.repositories.MovieRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MovieDao {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieDao(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Optional<Movie> findById(Integer id) {
        return movieRepository.findById(id);
    }

    public List<Movie> findByGenreAndYearAndTitle(String genre, Integer year, String title) {
        return movieRepository.findByGenreIgnoreCaseAndYearAndTitleIgnoreCase(genre, year, title);
    }

    public Movie save(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteById(Integer id) {
        movieRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return movieRepository.existsById(id);
    }
}
