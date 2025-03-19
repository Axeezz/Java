package com.movio.moviolab.dao;

import com.movio.moviolab.models.User;
import com.movio.moviolab.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDao {

    private final UserRepository userRepository;

    @Autowired
    public UserDao(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public List<User> findByNameIgnoreCase(String name) {
        return userRepository.findByNameIgnoreCase(name);
    }

    public List<User> findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public List<User> findByNameIgnoreCaseAndEmailIgnoreCase(String name, String email) {
        return userRepository.findByNameIgnoreCaseAndEmailIgnoreCase(name, email);
    }

    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    public List<User> findUsersByMovieGenre(String genre) {
        return userRepository.findUsersByMovieGenre(genre);
    }

    public List<User> findUsersByMovieGenreNative(String genre) {
        return userRepository.findUsersByMovieGenreNative(genre);
    }
}
