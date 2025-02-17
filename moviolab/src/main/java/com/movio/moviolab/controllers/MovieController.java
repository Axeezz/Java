package com.movio.moviolab.controllers;

import com.movio.moviolab.Movie;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class MovieController {
    private final List<Movie> movies = List.of(
            new Movie("Inception", Movie.GENRE_SCIFI, 2010),
            new Movie("Titanic", Movie.GENRE_ROMANCE, 1997),
            new Movie("Leon: The Professional", Movie.GENRE_ACTION, 1994),
            new Movie("The Terminator", Movie.GENRE_SCIFI, 1984),
            new Movie("The Exorcist", Movie.GENRE_HORROR, 1973),
            new Movie("Lost In Translation", Movie.GENRE_COMEDY, 2003),
            new Movie("E.T. The Extra Terrestrial",
                    Movie.GENRE_ADVENTURE, 1982),
            new Movie("In The Mood For Love", Movie.GENRE_DRAMA, 2000),
            new Movie("Forrest Gump", Movie.GENRE_DRAMA, 1994),
            new Movie("Spirited Away", Movie.GENRE_ADVENTURE, 2001),
            new Movie("Ghostbusters", Movie.GENRE_ACTION, 1984),
            new Movie("The Lord Of The Rings", Movie.GENRE_FANTASY, 2001),
            new Movie("Back To The Future", Movie.GENRE_ADVENTURE, 1985),
            new Movie("Brokeback Mountain", Movie.GENRE_DRAMA, 2005),
            new Movie("Paddington 2", Movie.GENRE_ADVENTURE, 2017),
            new Movie("The Matrix", Movie.GENRE_SCIFI, 1999),
            new Movie("Reservoir Dogs", Movie.GENRE_CRIME, 1992),
            new Movie("The Godfather", Movie.GENRE_CRIME, 1972)
    );
    @GetMapping("/movies")
    public List<Movie> getMovies(
            @RequestParam(name = "genre", required = false) final String genre,
            @RequestParam(name = "year", required = false) final Integer year
    ) {
        return movies.stream()
                .filter(movie -> (genre == null
                        || movie.getGenre().equalsIgnoreCase(genre))
                        && (year == null || movie.getYear().equals(year)))
                .toList();
    }

    @GetMapping("/movies/{movieName}")
    public Movie getMovieByMovieName(@PathVariable final String movieName) {
        return movies.stream()
                .filter(movie
                        -> movie.getMovieName().equalsIgnoreCase(movieName))
                .findFirst()
                .orElse(null);
    }
}
