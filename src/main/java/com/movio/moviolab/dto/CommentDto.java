package com.movio.moviolab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CommentDto {
    private Integer id;
    private String content;
    private Integer userId;
    private Integer movieId;
}
