package com.movio.moviolab.services;

import com.movio.moviolab.cache.InMemoryCache;
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

    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "User with this name or email already exists: ";

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
        User savedUser = userDao.save(user);

        inMemoryCache.removeAll();

        return convertToDto(savedUser);
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

        inMemoryCache.removeAll();

        return ResponseEntity.noContent().build();
    }

    @Transactional
    public UserDto updateUser(Integer id, UserDto updatedUserDto) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        user.setName(updatedUserDto.getName());
        user.setEmail(updatedUserDto.getEmail());
        user.setPassword(updatedUserDto.getPassword());

        User updatedUser = userDao.save(user);

        inMemoryCache.removeAll();

        return convertToDto(updatedUser);
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

        User updatedUser = userDao.save(user);

        inMemoryCache.removeAll();

        return convertToDto(updatedUser);
    }

    public List<CommentDto> getCommentsByUserId(Integer id) {
        User user = userDao.findById(id).orElseThrow(()
                -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
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


    private UserDto convertToDto(User user) {
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
