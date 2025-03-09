package com.movio.moviolab.services;

import com.movio.moviolab.dao.UserDao;
import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.User;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "User with this name or email already exists: ";

    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
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
    public void deleteUserById(Integer id) {
        if (!userDao.existsById(id)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id);
        }
        userDao.deleteById(id);
    }

    @Transactional
    public User updateUser(Integer id, User updatedUser) {
        return userDao.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            return userDao.save(user);
        }).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
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
