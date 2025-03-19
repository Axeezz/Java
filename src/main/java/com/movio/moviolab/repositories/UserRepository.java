package com.movio.moviolab.repositories;

import com.movio.moviolab.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findByNameIgnoreCase(String name);

    List<User> findByEmailIgnoreCase(String email);

    List<User> findByNameIgnoreCaseAndEmailIgnoreCase(String name, String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.movies m WHERE m.genre = :genre")
    List<User> findUsersByMovieGenre(@Param("genre") String genre);

    @Query(value = "SELECT DISTINCT u.* FROM user u "
            + "JOIN movie_user mu ON u.id = mu.user_id "
            + "JOIN movie m ON mu.movie_id = m.id "
            + "WHERE m.genre = :genre", nativeQuery = true)
    List<User> findUsersByMovieGenreNative(@Param("genre") String genre);


}
