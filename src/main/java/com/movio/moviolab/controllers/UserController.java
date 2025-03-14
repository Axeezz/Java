package com.movio.moviolab.controllers;

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

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email) {
        return userService.getUsers(name, email);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> getCommentsByUserId(@PathVariable final Integer id) {
        return userService.getCommentsByUserId(id);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        try {
            return userService.addUser(userDto);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@Valid @PathVariable Integer id,
                              @RequestBody UserDto updatedUserDto) {
        return userService.updateUser(id, updatedUserDto);
    }

    @PatchMapping("/{id}")
    public UserDto patchUser(@Valid @PathVariable Integer id, @RequestBody UserDto partialUserDto) {
        return userService.patchUser(id, partialUserDto);
    }
}
