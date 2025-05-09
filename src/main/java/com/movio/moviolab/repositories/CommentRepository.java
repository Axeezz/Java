package com.movio.moviolab.repositories;

import com.movio.moviolab.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    boolean existsByUserIdAndMovieIdAndContent(Integer userId, Integer movieId, String content);


}
