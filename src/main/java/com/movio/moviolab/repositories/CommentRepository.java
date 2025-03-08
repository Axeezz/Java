package com.movio.moviolab.repositories;

import com.movio.moviolab.models.Comment;
import com.movio.moviolab.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findCommentsByMovieId(Integer movieId);
}
