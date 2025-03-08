package com.movio.moviolab.controllers;

import com.movio.moviolab.models.Comment;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/movies")
final class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getMovies(
            @RequestParam(name = "genre", required = false) final String genre,
            @RequestParam(name = "year", required = false) final Integer year,
            @RequestParam(name = "title", required = false) final String title
    ) {
        return movieService.getMovies(genre, year, title);
    }

    @GetMapping("/{id}")
    public Movie getMovieById(@PathVariable final Integer id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/comments/{id}")
    public List<Comment> getCommentsByMovieId(@PathVariable final Integer id) {
        return movieService.getCommentsByMovieId(id);
    }

    // Метод для обработки POST-запроса на добавление нового фильма
    @PostMapping
    public Movie addMovie(@RequestBody Movie movie) {
        return movieService.addMovie(movie);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovieById(id);
    }

    @PutMapping("/{id}")
    public Movie updateMovie(@PathVariable Integer id, @RequestBody Movie updatedMovie) {
        return movieService.updateMovie(id, updatedMovie);
    }

    @PatchMapping("/{id}")
    public Movie patchMovie(@PathVariable Integer id, @RequestBody Movie partialMovie) {
        return movieService.patchMovie(id, partialMovie);
    }
}