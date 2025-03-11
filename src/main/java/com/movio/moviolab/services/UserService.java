package com.movio.moviolab.services;

import com.movio.moviolab.dao.MovieDao;
import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.exceptions.MovieNotFoundException;
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

    public List<User> getUsers(String name, String email) {
        if (name != null && email != null) {
            return userDao.findByNameIgnoreCaseAndEmailIgnoreCase(name, email);
        } else if (name != null) {
            return userDao.findByNameIgnoreCase(name);
        } else if (email != null) {
            return userDao.findByEmailIgnoreCase(email);
        } else {
            return userDao.findAll();
        }
    }

    public User getUserById(Integer id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
    }

    public User addUser(User user) {
        if (!userDao.findByNameIgnoreCase(user.getName()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getName());
        }
        if (!userDao.findByEmailIgnoreCase(user.getEmail()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getEmail());
        }
        return userDao.save(user);
    }

    @Transactional
    public ResponseEntity<Void> deleteUserById(Integer id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_MESSAGE + id));

        // Удаление связей с фильмами: обнуляем поле movies у каждого фильма
        for (Movie movie : user.getMovies()) {
            movie.getUsers().remove(user);
            movieDao.save(movie); // Сохраняем изменения в фильмах
        }

        // Удаление пользователя
        userDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }




    @Transactional
    public User updateUser(Integer id, User updatedUser) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        // Обновление полей пользователя
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());

        // Сохранение изменений (не обязательно при работе с JPA, если сущность отслеживается)
        return user;  // После завершения транзакции, изменения будут автоматически сохранены
    }


    public User patchUser(Integer id, User partialUser) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        if (partialUser.getName() != null) {
            user.setName(partialUser.getName());
        }
        if (partialUser.getEmail() != null) {
            user.setEmail(partialUser.getEmail());
        }
        if (partialUser.getPassword() != null) {
            user.setPassword(partialUser.getPassword());
        }

        return userDao.save(user);
    }

    public List<Comment> getCommentsByUserId(Integer id) {
        User user = userDao.findById(id).orElseThrow(()
                -> new MovieNotFoundException(USER_NOT_FOUND_MESSAGE + id));
        return user.getComments();
    }
}
