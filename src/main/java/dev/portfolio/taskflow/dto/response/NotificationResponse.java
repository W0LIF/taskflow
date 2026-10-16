package dev.portfolio.taskflow.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String message,
        boolean isRead,
        Long taskId,
        Long projectId,
        LocalDateTime createdAt
) {}
