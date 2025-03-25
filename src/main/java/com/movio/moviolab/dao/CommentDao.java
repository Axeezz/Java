package com.movio.moviolab.dao;

import com.movio.moviolab.models.Comment;
import com.movio.moviolab.repositories.CommentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommentDao {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentDao(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Optional<Comment> findById(Integer id) {
        return commentRepository.findById(id);
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }

    public boolean existsByUserIdAndMovieIdAndContent(Integer userId,
                                                      Integer movieId, String content) {
        return commentRepository
                .existsByUserIdAndMovieIdAndContent(userId, movieId, content);
    }
}
