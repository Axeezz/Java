package com.movio.moviolab.repositories;

import com.movio.moviolab.models.Movie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByGenreIgnoreCaseAndYearAndTitleIgnoreCase(String genre,
                                                              Integer year, String title);
}
