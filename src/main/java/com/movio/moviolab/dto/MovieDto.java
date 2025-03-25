package com.movio.moviolab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MovieDto {

    private Integer id;
    private String title;
    private String genre;
    private Integer year;
    private List<UserDto> users;
    private List<CommentDto> comments;
}
