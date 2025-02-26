package com.movio.moviolab.controllers;

import com.movio.moviolab.models.Movie;
import com.movio.moviolab.services.MovieService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies")
    public List<Movie> getMovies(
            @RequestParam(name = "genre", required = false) final String genre,
            @RequestParam(name = "year", required = false) final Integer year
    ) {
        return movieService.getMovies(genre, year);
    }

    @GetMapping("/movies/withname/{name}")
    public Movie getMovieByMovieName(@PathVariable final String name) {
        return movieService.getMovieByMovieName(name);
    }

    @GetMapping("/movies/withid/{id}")
    public Movie getMovieById(@PathVariable final Integer id) {
        return movieService.getMovieById(id);
    }
}
