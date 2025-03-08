package com.movio.moviolab.services;

import com.movio.moviolab.exceptions.UserNotFoundException;
import com.movio.moviolab.models.User;
import com.movio.moviolab.repositories.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found: ";
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "User with this name or email already exists: ";

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers(String name, String email) {
        if (name != null && email != null) {
            return userRepository.findByNameIgnoreCaseAndEmailIgnoreCase(name, email);
        } else if (name != null) {
            return userRepository.findByNameIgnoreCase(name);
        } else if (email != null) {
            return userRepository.findByEmailIgnoreCase(email);
        } else {
            return userRepository.findAll();
        }
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
    }

    public User addUser(User user) {
        if (!userRepository.findByNameIgnoreCase(user.getName()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getName());
        }
        if (!userRepository.findByEmailIgnoreCase(user.getEmail()).isEmpty()) {
            throw new IllegalArgumentException(USER_ALREADY_EXISTS_MESSAGE + user.getEmail());
        }
        return userRepository.save(user);
    }

    public void deleteUserById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id);
        }
        userRepository.deleteById(id);
    }

    public User updateUser(Integer id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            return userRepository.save(user);
        }).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
    }

    public User patchUser(Integer id, User partialUser) {
        User user = userRepository.findById(id)
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

        return userRepository.save(user);
    }
}