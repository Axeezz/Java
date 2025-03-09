package com.movio.moviolab.repositories;

import com.movio.moviolab.models.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByGenreIgnoreCase(String genre);

    List<Movie> findByYear(Integer year);

    List<Movie> findByTitleIgnoreCase(String title);

    List<Movie> findByGenreIgnoreCaseAndTitleIgnoreCase(String genre, String title);

    List<Movie> findByYearAndTitleIgnoreCase(Integer year, String title);

    List<Movie> findByGenreIgnoreCaseAndYearAndTitleIgnoreCase(String genre,
                                                               Integer year, String title);
}
