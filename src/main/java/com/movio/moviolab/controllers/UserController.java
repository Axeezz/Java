package com.movio.moviolab.controllers;

import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.services.UserService;
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
public class UserController {

    private final UserService userService;
    private final UserDao userDao;

    @Autowired
    public UserController(UserService userService, UserDao userDao) {
        this.userService = userService;
        this.userDao = userDao;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email) {
        List<UserDto> users = userService.getUsers(name, email);
        return users.isEmpty() ? ResponseEntity.status(404).body(users) : ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByUserId(@PathVariable Integer id) {
        List<CommentDto> comments = userService.getCommentsByUserId(id);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto userDto) {
        UserDto addedUser = userService.addUser(userDto);
        return ResponseEntity.status(201).body(addedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id,
                                              @Valid @RequestBody UserDto updatedUserDto) {
        UserDto updatedUser = userService.updateUser(id, updatedUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> patchUser(@PathVariable Integer id,
                                             @Valid @RequestBody UserDto partialUserDto) {
        UserDto updatedUser = userService.patchUser(id, partialUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/by-movie-genre")
    public ResponseEntity<List<UserDto>> getUsersByMovieGenre(@RequestParam String genre) {
        List<UserDto> users = userService.getUsersByGenreFromCacheOrDb(genre,
                userDao::findUsersByMovieGenre);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-movie-genre-native")
    public ResponseEntity<List<UserDto>> getUsersByMovieGenreNative(@RequestParam String genre) {
        List<UserDto> users = userService.getUsersByGenreFromCacheOrDb(genre,
                userDao::findUsersByMovieGenreNative);
        return ResponseEntity.ok(users);
    }
}
