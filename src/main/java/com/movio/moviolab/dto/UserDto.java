package com.movio.moviolab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class UserDto {

    private Integer id;
    private String name;
    private String email;
    private String password;

    private List<CommentDto> comments = new ArrayList<>();
    private List<MovieDto> movies = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(id, userDto.id)
                && Objects.equals(name, userDto.name)
                && Objects.equals(email, userDto.email)
                && Objects.equals(password, userDto.password)
                && Objects.equals(comments, userDto.comments)
                && Objects.equals(movies, userDto.movies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, comments, movies);
    }
}
