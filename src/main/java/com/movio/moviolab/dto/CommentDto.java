package com.movio.moviolab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CommentDto {
    private Integer id;

    @NotBlank(message = "Comment is mandatory")
    @Size(min = 2, max = 500, message = "Comment must be between 2 and 500 characters")
    private String content;
    private Integer userId;
    private Integer movieId;
}
