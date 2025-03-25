package com.movio.moviolab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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
    private List<CommentDto> comments;
    private List<MovieDto> movies;
}
