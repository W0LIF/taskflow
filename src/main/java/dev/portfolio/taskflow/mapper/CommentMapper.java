package dev.portfolio.taskflow.mapper;

import dev.portfolio.taskflow.dto.response.CommentResponse;
import dev.portfolio.taskflow.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    private final UserMapper userMapper;

    public CommentResponse toResponse(Comment comment){
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                userMapper.toSummary(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
