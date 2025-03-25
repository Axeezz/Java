package com.movio.moviolab.controllers;

import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.services.MovieService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieDto>> getMovies(
            @RequestParam(name = "genre", required = false) final String genre,
            @RequestParam(name = "year", required = false) final Integer year,
            @RequestParam(name = "title", required = false) final String title
    ) {
        List<MovieDto> movies = movieService.getMovies(genre, year, title);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable final Integer id) {
        MovieDto movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByMovieId(@PathVariable final Integer id) {
        List<CommentDto> comments = movieService.getCommentsByMovieId(id);
        return ResponseEntity.ok(comments);

    }

    @PostMapping
    public ResponseEntity<MovieDto> addMovie(@RequestBody MovieDto movieDto) {
        // Валидация данных вручную
        MovieDto newMovie = movieService.addMovie(movieDto);
        return ResponseEntity.status(201).body(newMovie);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovieById(id);
        return ResponseEntity.ok("Movie deleted successfully"); // Success
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Integer id,
                                                @RequestBody MovieDto updatedMovieDto) {
        MovieDto updatedMovie = movieService.updateMovie(id, updatedMovieDto);
        return ResponseEntity.ok(updatedMovie); // Movie updated
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MovieDto> patchMovie(@PathVariable Integer id,
                                               @RequestBody MovieDto partialMovieDto) {
        MovieDto patchedMovie = movieService.patchMovie(id, partialMovieDto);
        return ResponseEntity.ok(patchedMovie); // Movie patched
    }

    @PostMapping("/{movieId}/users/{userId}")
    public ResponseEntity<String> addUserToMovie(@PathVariable Integer movieId,
                                                 @PathVariable Integer userId) {
        return movieService.addUserToMovie(movieId, userId);
    }

    @DeleteMapping("/{movieId}/users/{userId}")
    public ResponseEntity<String> removeUserFromMovie(@PathVariable Integer movieId,
                                                    @PathVariable Integer userId) {
        return movieService.removeUserFromMovie(movieId, userId);
    }

    @GetMapping("/{movieId}/users")
    public ResponseEntity<List<UserDto>> getUsersForMovie(@PathVariable Integer movieId) {
        List<UserDto> users = movieService.getUsersForMovie(movieId);
        return ResponseEntity.ok(users);
    }
}
