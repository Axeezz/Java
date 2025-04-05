package com.movio.moviolab.controllers;

import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.services.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Movie Controller", description = "API для управления фильмами")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @Operation(summary = "Поиск фильма по фильтру",
            description = "Возвращает все фильмы по жанру, году или названию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Найденные фильмы возвращены"),
        @ApiResponse(responseCode = "404", description = "Фильмы не найдены"),
    })
    @GetMapping
    public ResponseEntity<List<MovieDto>> getMovies(
        @RequestParam(name = "genre", required = false) final String genre,
        @RequestParam(name = "year", required = false) final Integer year,
        @RequestParam(name = "title", required = false) final String title
    ) {
        List<MovieDto> movies = movieService.getMovies(genre, year, title);
        return movies.isEmpty() ? ResponseEntity.status(404).body(movies)
                : ResponseEntity.ok(movies);
    }

    @Operation(summary = "Получение фильма по ID", description = "Возвращает фильм по ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фильм найден"),
        @ApiResponse(responseCode = "404", description = "Фильм с таким ID не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable final Integer id) {
        MovieDto movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @Operation(summary = "Получение комментариев для фильма по его ID",
            description = "Возвращает комментарии для фильм с ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарии возвращены"),
        @ApiResponse(responseCode = "404", description = "Фильм с таким ID не найден")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByMovieId(@PathVariable final Integer id) {
        List<CommentDto> comments = movieService.getCommentsByMovieId(id);
        return ResponseEntity.ok(comments);

    }

    @Operation(summary = "Добавление нового фильма", description = "Создает новый фильм")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Фильм добавлен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
        @ApiResponse(responseCode = "404", description = "Такой фильм уже сущетвует")
    })
    @PostMapping
    public ResponseEntity<MovieDto> addMovie(@RequestBody MovieDto movieDto) {
        MovieDto newMovie = movieService.addMovie(movieDto);
        return ResponseEntity.status(201).body(newMovie);
    }

    @Operation(summary = "Удаление фильма по его ID", description = "Удалет фильм с ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фильм удален успешно"),
        @ApiResponse(responseCode = "404", description = "Фильм с таким ID не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovieById(id);
        return ResponseEntity.ok("Фильм удален успешно");
    }

    @Operation(summary = "Изменение фильма", description = "Изменяет всю информацию о фильме")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фильм изменен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Integer id,
                                                @RequestBody MovieDto updatedMovieDto) {
        MovieDto updatedMovie = movieService.updateMovie(id, updatedMovieDto);
        return ResponseEntity.ok(updatedMovie);
    }

    @Operation(summary = "Изменение фильма", description = "Изменяет информацию о фильме")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Фильм изменен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
    @PatchMapping("/{id}")
    public ResponseEntity<MovieDto> patchMovie(@PathVariable Integer id,
                                               @RequestBody MovieDto partialMovieDto) {
        MovieDto patchedMovie = movieService.patchMovie(id, partialMovieDto);
        return ResponseEntity.ok(patchedMovie);
    }

    @Operation(summary = "Добавление пользователя к фильму",
            description = "Связывает пользователя и фильм")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Пользователь добавлен успешно"),
        @ApiResponse(responseCode = "404", description =
                "Такой пользователь у фильма уже существует"),
    })
    @PostMapping("/{movieId}/users/{userId}")
    public ResponseEntity<String> addUserToMovie(@PathVariable Integer movieId,
                                                 @PathVariable Integer userId) {
        return movieService.addUserToMovie(movieId, userId);
    }

    @Operation(summary = "Удаление пользователя из фильма",
            description = "Удаляет связь между пользователем и фильмом")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Пользователь удален успешно"),
        @ApiResponse(responseCode = "404", description = "Пользователь или фильм ненайден")
    })
    @DeleteMapping("/{movieId}/users/{userId}")
    public ResponseEntity<String> removeUserFromMovie(@PathVariable Integer movieId,
                                                    @PathVariable Integer userId) {
        return movieService.removeUserFromMovie(movieId, userId);
    }

    @Operation(summary = "Поиск всех пользоватлей для фильма",
            description = "Возвращает всех пользователей связанных с фильмом")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список пользователей возвращен"),
        @ApiResponse(responseCode = "404", description = "Фильм не найден"),
    })
    @GetMapping("/{movieId}/users")
    public ResponseEntity<List<UserDto>> getUsersForMovie(@PathVariable Integer movieId) {
        List<UserDto> users = movieService.getUsersForMovie(movieId);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Добавление нового фильма", description = "Создает новый фильм")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Фильм(ы) добавлен(ы) успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный(или пустой) запрос"),
        @ApiResponse(responseCode = "404", description = "Такие фильмы уже существуют")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<MovieDto>> addMoviesBulk(@RequestBody List<MovieDto> movieDtos) {
        List<MovieDto> savedMovies = movieService.addMoviesBulk(movieDtos);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovies);
    }
}
