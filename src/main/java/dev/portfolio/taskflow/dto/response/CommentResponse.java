package dev.portfolio.taskflow.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long taskId,
        UserSummaryResponse author,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
