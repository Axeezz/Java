package com.movio.moviolab.models;

public class Movie {
    public static final String GENRE_DRAMA = "Drama";
    public static final String GENRE_ADVENTURE = "Adventure";
    public static final String GENRE_SCIFI = "Sci-Fi";
    public static final String GENRE_ROMANCE = "Romance";
    public static final String GENRE_ACTION = "Action";
    public static final String GENRE_COMEDY = "Comedy";
    public static final String GENRE_FANTASY = "Fantasy";
    public static final String GENRE_HORROR = "Horror";
    public static final String GENRE_CRIME = "Crime";

    private final int id;
    private final String name;
    private final String genre;
    private final int year;

    public Movie(final int inputId, final String inputName,
                 final String inputGenre, final int inputYear) {
        this.id = inputId;
        this.name = inputName;
        this.genre = inputGenre;
        this.year = inputYear;
    }

    public final Integer getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getGenre() {
        return genre;
    }

    public final Integer getYear() {
        return year;
    }
}
