package com.movio.moviolab.controllers;

import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/users")
@Tag(name = "User Controller", description = "API для управления фильмами")
public class UserController {

    private final UserService userService;
    private final UserDao userDao;

    @Autowired
    public UserController(UserService userService, UserDao userDao) {
        this.userService = userService;
        this.userDao = userDao;
    }

    @Operation(summary = "Поиск пользователя по фильтру",
            description = "Возвращает все пользователя по имени или почте")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Найденные пользователи возвращены"),
        @ApiResponse(responseCode = "404", description = "Пользовател не найдены"),
    })
    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email) {
        List<UserDto> users = userService.getUsers(name, email);
        return users.isEmpty() ? ResponseEntity.status(404).body(users) : ResponseEntity.ok(users);
    }

    @Operation(summary = "Поиск фильма по ID", description = "Возвращает пользователя по его ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь найден"),
        @ApiResponse(responseCode = "404", description = "Такого пользователя не существует")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Получение комментариев пользователя по его ID",
            description = "Возвращает все комментарии пользователя с ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарии возвращены"),
        @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByUserId(@PathVariable Integer id) {
        List<CommentDto> comments = userService.getCommentsByUserId(id);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "Добавление нового пользователя",
            description = "Добавляет нового пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Пользователь добавлен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
        @ApiResponse(responseCode = "404", description = "Такой пользователь уже сущетвует")
    })
    @PostMapping
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto userDto) {
        UserDto addedUser = userService.addUser(userDto);
        return ResponseEntity.status(201).body(addedUser);
    }

    @Operation(summary = "Удаление пользователя", description = "Удаляет пользователя по его ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Пользователь удален успешно"),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }

    @Operation(summary = "Изменение пользователя",
            description = "Изменяет всю информацию о пользователе")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь изменен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id,
                                              @Valid @RequestBody UserDto updatedUserDto) {
        UserDto updatedUser = userService.updateUser(id, updatedUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Изменение пользоватлея",
            description = "Изменяет информацию о пользователе")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь изменен успешно"),
        @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> patchUser(@PathVariable Integer id,
                                             @Valid @RequestBody UserDto partialUserDto) {
        UserDto updatedUser = userService.patchUser(id, partialUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Получение пользователей по жанру фильма",
            description = "Возвращает пользователей, которые смотрели фильм определенного жанра")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователи возвращены"),
        @ApiResponse(responseCode = "404", description =
                "Нет пользователей смотревших фильмы с таким жанром")
    })
    @GetMapping("/by-movie-genre")
    public ResponseEntity<List<UserDto>> getUsersByMovieGenre(@RequestParam String genre) {
        List<UserDto> users = userService.getUsersByGenreFromCacheOrDb(genre,
                userDao::findUsersByMovieGenre);
        return users.isEmpty() ? ResponseEntity.status(404).body(users) : ResponseEntity.ok(users);
    }

    @Operation(summary = "Получение пользователей по жанру фильма",
            description = "Возвращает пользователей, которые смотрели фильм определенного жанра")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователи возвращены"),
        @ApiResponse(responseCode = "404", description =
                "Нет пользователей смотревших фильмы с таким жанром")
    })
    @GetMapping("/by-movie-genre-native")
    public ResponseEntity<List<UserDto>> getUsersByMovieGenreNative(@RequestParam String genre) {
        List<UserDto> users = userService.getUsersByGenreFromCacheOrDb(genre,
                userDao::findUsersByMovieGenreNative);
        return users.isEmpty() ? ResponseEntity.status(404).body(users) : ResponseEntity.ok(users);
    }
}
