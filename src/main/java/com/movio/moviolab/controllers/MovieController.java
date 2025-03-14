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
    public List<MovieDto> getMovies(
            @RequestParam(name = "genre", required = false) final String genre,
            @RequestParam(name = "year", required = false) final Integer year,
            @RequestParam(name = "title", required = false) final String title
    ) {
        return movieService.getMovies(genre, year, title);
    }

    @GetMapping("/{id}")
    public MovieDto getMovieById(@PathVariable final Integer id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> getCommentsByMovieId(@PathVariable final Integer id) {
        return movieService.getCommentsByMovieId(id);
    }

    @PostMapping
    public MovieDto addMovie(@RequestBody MovieDto movieDto) {
        return movieService.addMovie(movieDto);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovieById(id);
    }

    @PutMapping("/{id}")
    public MovieDto updateMovie(@PathVariable Integer id, @RequestBody MovieDto updatedMovieDto) {
        return movieService.updateMovie(id, updatedMovieDto);
    }

    @PatchMapping("/{id}")
    public MovieDto patchMovie(@PathVariable Integer id, @RequestBody MovieDto partialMovieDto) {
        return movieService.patchMovie(id, partialMovieDto);
    }

    @PostMapping("/{movieId}/users/{userId}")
    public ResponseEntity<String> addUserToMovie(@PathVariable Integer movieId,
                                               @PathVariable Integer userId) {
        return movieService.addUserToMovie(movieId, userId);
    }

    @DeleteMapping("/{movieId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromMovie(@PathVariable Integer movieId,
                                                    @PathVariable Integer userId) {
        return movieService.removeUserFromMovie(movieId, userId);
    }

    @GetMapping("/{movieId}/users")
    public ResponseEntity<List<UserDto>> getUsersForMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getUsersForMovie(movieId));
    }
}
