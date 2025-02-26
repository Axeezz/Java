package com.movio.moviolab.services;

import com.movio.moviolab.exceptions.MovieNotFoundException;
import com.movio.moviolab.models.Movie;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MovieService {
    private final List<Movie> movies = List.of(
            new Movie(1, "Inception", Movie.GENRE_SCIFI, 2010),
            new Movie(2, "Titanic", Movie.GENRE_ROMANCE, 1997),
            new Movie(3, "Leon: The Professional", Movie.GENRE_ACTION, 1994),
            new Movie(4, "The Terminator", Movie.GENRE_SCIFI, 1984),
            new Movie(5, "The Exorcist", Movie.GENRE_HORROR, 1973),
            new Movie(6, "Lost In Translation", Movie.GENRE_COMEDY, 2003),
            new Movie(7, "E.T. The Extra Terrestrial", Movie.GENRE_ADVENTURE, 1982),
            new Movie(8, "In The Mood For Love", Movie.GENRE_DRAMA, 2000),
            new Movie(9, "Forrest Gump", Movie.GENRE_DRAMA, 1994),
            new Movie(10, "Spirited Away", Movie.GENRE_ADVENTURE, 2001),
            new Movie(11, "Ghostbusters", Movie.GENRE_ACTION, 1984),
            new Movie(12, "The Lord Of The Rings", Movie.GENRE_FANTASY, 2001),
            new Movie(13, "Back To The Future", Movie.GENRE_ADVENTURE, 1985),
            new Movie(14, "Brokeback Mountain", Movie.GENRE_DRAMA, 2005),
            new Movie(15, "Paddington 2", Movie.GENRE_ADVENTURE, 2017),
            new Movie(16, "The Matrix", Movie.GENRE_SCIFI, 1999),
            new Movie(17, "Reservoir Dogs", Movie.GENRE_CRIME, 1992),
            new Movie(18, "The Godfather", Movie.GENRE_CRIME, 1972)
    );

    public List<Movie> getMovies(String genre, Integer year) {
        List<Movie> filteredMovies = movies.stream()
                .filter(movie -> (genre == null
                        || movie.getGenre().equalsIgnoreCase(genre))
                        && (year == null || movie.getYear().equals(year)))
                .toList();

        if (filteredMovies.isEmpty()) {
            throw new MovieNotFoundException("No movies found with the given parameters.");
        }

        return filteredMovies;
    }

    public Movie getMovieByMovieName(String name) {
        return movies.stream()
                .filter(movie -> movie.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException("Movie not found: " + name));
    }

    public Movie getMovieById(Integer id) {
        return movies.stream()
                .filter(movie -> movie.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new MovieNotFoundException("Movie not found: " + id));
    }
}