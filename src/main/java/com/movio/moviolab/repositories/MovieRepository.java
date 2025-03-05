package com.movio.moviolab.repositories;

import com.movio.moviolab.models.Movie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByGenreIgnoreCase(String genre);

    List<Movie> findByYear(Integer year);

    List<Movie> findByGenreIgnoreCaseAndYear(String genre, Integer year);

    Optional<Movie> findByTitleIgnoreCase(String name);
}