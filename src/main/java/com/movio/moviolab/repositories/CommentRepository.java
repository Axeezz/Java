package com.movio.moviolab.repositories;

import com.movio.moviolab.models.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findCommentsByMovieId(Integer movieId);

    List<Comment> findCommentsByUserId(Integer userId);
}
