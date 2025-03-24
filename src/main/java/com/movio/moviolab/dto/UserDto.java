package com.movio.moviolab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class UserDto {

    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters")

    private String password;
    private List<CommentDto> comments;
    private List<MovieDto> movies;
}
