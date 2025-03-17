package com.movio.moviolab.services;

import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "User with this name or email already exists: ";

    private final UserDao userDao;
    private final MovieDao movieDao;

    @Autowired
    public UserService(UserDao userDao, MovieDao movieDao) {
        this.userDao = userDao;
        this.movieDao = movieDao;
    }

    public List<UserDto> getUsers(String name, String email) {
        List<User> users;
        if (name != null && email != null) {
            users = userDao.findByNameIgnoreCaseAndEmailIgnoreCase(name, email);
        } else if (name != null) {
            users = userDao.findByNameIgnoreCase(name);
        } else if (email != null) {
            users = userDao.findByEmailIgnoreCase(email);
        } else {
            users = userDao.findAll();
        }
        return users.stream().map(this::convertToDto).toList();
    }

    public UserDto getUserById(Integer id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
        return convertToDto(user);
    }

    public UserDto addUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        if (!userDao.findByNameIgnoreCase(user.getName()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getName());
        }
        if (!userDao.findByEmailIgnoreCase(user.getEmail()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getEmail());
        }
        return convertToDto(userDao.save(user));
    }

    @Transactional
    public ResponseEntity<Void> deleteUserById(Integer id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MESSAGE + id));

        for (Movie movie : user.getMovies()) {
            movie.getUsers().remove(user);
            movieDao.save(movie);
        }

        userDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public UserDto updateUser(Integer id, UserDto updatedUserDto) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        user.setName(updatedUserDto.getName());
        user.setEmail(updatedUserDto.getEmail());
        user.setPassword(updatedUserDto.getPassword());

        return convertToDto(user);
    }

    @Transactional
    public UserDto patchUser(Integer id, UserDto partialUserDto) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        if (partialUserDto.getName() != null) {
            user.setName(partialUserDto.getName());
        }
        if (partialUserDto.getEmail() != null) {
            user.setEmail(partialUserDto.getEmail());
        }
        if (partialUserDto.getPassword() != null) {
            user.setPassword(partialUserDto.getPassword());
        }

        return convertToDto(userDao.save(user));
    }

    public List<CommentDto> getCommentsByUserId(Integer id) {
        User user = userDao.findById(id).orElseThrow(()
                -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
        return user.getComments().stream().map(this::convertToDto).toList();
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());

        if (user.getComments() != null) {
            List<CommentDto> commentDtos = user.getComments().stream()
                    .map(this::convertToDto)
                    .toList();
            userDto.setComments(commentDtos);
        }

        if (user.getMovies() != null) {
            List<MovieDto> movieDtos = user.getMovies().stream()
                    .map(this::convertToDto)
                    .toList();
            userDto.setMovies(movieDtos);
        }

        return userDto;
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setUserId(comment.getUserId());
        commentDto.setMovieId(comment.getMovieId());
        return commentDto;
    }

    private MovieDto convertToDto(Movie movie) {
        MovieDto movieDto = new MovieDto();
        movieDto.setId(movie.getId());
        movieDto.setTitle(movie.getTitle());
        movieDto.setGenre(movie.getGenre());
        movieDto.setYear(movie.getYear());
        return movieDto;
    }

    private User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        return user;
    }
}
