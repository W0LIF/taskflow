package dev.portfolio.taskflow.dto.response;

import dev.portfolio.taskflow.enums.TaskPriority;
import dev.portfolio.taskflow.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UserSummaryResponse assignee,
        UserSummaryResponse reporter,
        LocalDateTime dueDate,
        List<LabelResponse> labels,
        long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
