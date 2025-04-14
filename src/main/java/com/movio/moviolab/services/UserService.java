package com.movio.moviolab.services;

import com.movio.moviolab.cache.InMemoryCache;
import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.dto.CommentDto;
import com.movio.moviolab.dto.MovieDto;
import com.movio.moviolab.dto.UserDto;
import com.movio.moviolab.exceptions.UserException;
import com.movio.moviolab.exceptions.ValidationException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import com.movio.moviolab.models.User;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь не найден: ";
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "Пользователь с таким именем и почтой уже существует: ";

    private final UserDao userDao;
    private final MovieDao movieDao;
    private final InMemoryCache inMemoryCache;

    @Autowired
    public UserService(UserDao userDao, MovieDao movieDao, InMemoryCache inMemoryCache) {
        this.userDao = userDao;
        this.movieDao = movieDao;
        this.inMemoryCache = inMemoryCache;
    }

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String CACHE_KEY = "movie_genre_";

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

        if (users.isEmpty()) {
            throw new UserException(USER_NOT_FOUND_MESSAGE
                    + "пользователь с такими параметрами не существует");
        }

        return users.stream().map(this::convertToDto).toList();
    }

    public UserDto getUserById(Integer id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MESSAGE + id));
        return convertToDto(user);
    }

    public UserDto addUser(UserDto userDto) {

        validateUser(userDto, false);

        User user = convertToEntity(userDto);

        if (!userDao.findByNameIgnoreCase(user.getName()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getName());
        }
        if (!userDao.findByEmailIgnoreCase(user.getEmail()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getEmail());
        }

        User savedUser = userDao.save(user);

        return convertToDto(savedUser);
    }

    @Transactional
    public ResponseEntity<String> deleteUserById(Integer id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MESSAGE + id));

        for (Movie movie : user.getMovies()) {
            String genre = movie.getGenre();

            String key = CACHE_KEY + genre;
            inMemoryCache.remove(key);

            movie.getUsers().remove(user);
            movieDao.save(movie);
        }

        userDao.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    public UserDto updateUser(Integer id, UserDto updatedUserDto) {

        validateUser(updatedUserDto, false);

        User user = userDao.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MESSAGE + id));

        user.setName(updatedUserDto.getName());
        user.setEmail(updatedUserDto.getEmail());
        user.setPassword(updatedUserDto.getPassword());

        User updatedUser = userDao.save(user);

        for (Movie movie : user.getMovies()) {
            String genre = movie.getGenre();

            String key = CACHE_KEY + genre;
            inMemoryCache.remove(key);
        }

        return convertToDto(updatedUser);
    }

    @Transactional
    public UserDto patchUser(Integer id, UserDto partialUserDto) {

        validateUser(partialUserDto, true);

        User user = userDao.findById(id)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND_MESSAGE + id));

        if (partialUserDto.getName() != null) {
            user.setName(partialUserDto.getName());
        }
        if (partialUserDto.getEmail() != null) {
            user.setEmail(partialUserDto.getEmail());
        }
        if (partialUserDto.getPassword() != null) {
            user.setPassword(partialUserDto.getPassword());
        }

        User updatedUser = userDao.save(user);

        for (Movie movie : user.getMovies()) {
            String genre = movie.getGenre();

            String key = CACHE_KEY + genre;
            inMemoryCache.remove(key);
        }

        return convertToDto(updatedUser);
    }

    public List<CommentDto> getCommentsByUserId(Integer id) {
        User user = userDao.findById(id).orElseThrow(()
                -> new UserException(USER_NOT_FOUND_MESSAGE + id));

        if (user.getComments() == null) {
            user.setComments(new ArrayList<>());
        }

        return user.getComments().stream().map(this::convertToDto).toList();
    }

    public List<UserDto> getUsersByGenreFromCacheOrDb(String genre, Function<String,
            List<User>> findUsersByGenreFunction) {
        String key = CACHE_KEY + genre;

        if (inMemoryCache.contains(key)) {
            log.info("Извлечение пользователей для жанра '{}' из кеша", genre);

            Optional<Object> cachedData = inMemoryCache.get(key);

            if (cachedData.isPresent() && cachedData.get() instanceof List<?> cachedList) {
                if (!cachedList.isEmpty() && cachedList.getFirst() instanceof UserDto) {
                    @SuppressWarnings("unchecked")
                    List<UserDto> cachedUsers = (List<UserDto>) cachedList;
                    log.info("Возврат пользователей для жанра '{}'\n", genre);
                    return cachedUsers;
                } else {
                    log.warn("Данные для ключа '{}' имеют неожиданный тип или пусты.\n", key);
                    return Collections.emptyList();
                }
            } else {
                log.warn("Промах кеша или неверные данные ключа '{}'\n", key);
                return Collections.emptyList();
            }
        }

        log.info("Извлечение пользователей по жанру '{}' из базы данных", genre);
        List<User> users = findUsersByGenreFunction.apply(genre);

        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .toList();

        inMemoryCache.put(key, userDtos);

        return userDtos;
    }

    private void validateUser(UserDto userDto, boolean isPartial) {
        if (!isPartial) {
            validateNameAndEmail(userDto.getName(), userDto.getEmail());

            validatePassword(userDto.getPassword());
        } else {
            if (isInvalidName(userDto.getName())
                    && isInvalidEmail(userDto.getEmail())
                    && isInvalidPassword(userDto.getPassword())) {
                throw new ValidationException("Новый пользователь *invalid*");
            }
        }
    }

    private void validateNameAndEmail(String name, String email) {
        if (name == null || name.trim().isEmpty() || name.length() < 2 || name.length() > 50) {
            throw new ValidationException("Имя обязательно "
                    + "и должно быть от 2 до 50 символов");
        }
        if (email == null || email.trim().isEmpty() || isValidEmail(email)) {
            throw new ValidationException("Почта не введена или не соблюдается формат адреса");
        }

    }

    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Пароль обязателен");
        }
        if (password.length() < 5 || password.length() > 20) {
            throw new ValidationException("Длинна пароля должна быть от 5 до 20 символов");
        }
    }

    public boolean isInvalidName(String name) {
        return name == null || name.trim().isEmpty() || name.length() < 2 || name.length() > 50;
    }

    public boolean isInvalidEmail(String email) {
        return email == null || email.trim().isEmpty() || isValidEmail(email);
    }

    public boolean isInvalidPassword(String password) {
        return password == null || password.trim().isEmpty()
                || password.length() < 5 || password.length() > 20;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return !email.matches(emailRegex);
    }

    private UserDto convertToDto(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

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

    public MovieDto convertToDto(Movie movie) {
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
