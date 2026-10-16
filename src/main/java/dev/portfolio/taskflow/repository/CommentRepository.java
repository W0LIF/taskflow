package dev.portfolio.taskflow.repository;

import dev.portfolio.taskflow.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByTaskIdOrderByCreatedAtAsc(Long taskId);
}