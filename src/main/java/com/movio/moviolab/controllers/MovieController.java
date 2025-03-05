package com.movio.moviolab.controllers;

import com.movio.moviolab.models.Movie;
import com.movio.moviolab.services.MovieService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/movies/withname/{title}")
    public Movie getMovieByMovieName(@PathVariable final String title) {
        return movieService.getMovieByTitle(title);
    }

    @GetMapping("/movies/{id}")
    public Movie getMovieById(@PathVariable final Integer id) {
        return movieService.getMovieById(id);
    }

    // Метод для обработки POST-запроса на добавление нового фильма
    @PostMapping("/movies")
    public Movie addMovie(@RequestBody Movie movie) {
        return movieService.addMovie(movie);
    }

    @DeleteMapping("/movies/{id}")
    public void deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovieById(id);
    }

    @PutMapping("/movies/{id}")
    public Movie updateMovie(@PathVariable Integer id, @RequestBody Movie updatedMovie) {
        return movieService.updateMovie(id, updatedMovie);
    }

    @PatchMapping("/movies/{id}")
    public Movie patchMovie(@PathVariable Integer id, @RequestBody Movie partialMovie) {
        return movieService.patchMovie(id, partialMovie);
    }

}
