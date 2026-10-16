package dev.portfolio.taskflow.dto.request;

import dev.portfolio.taskflow.enums.TaskPriority;
import dev.portfolio.taskflow.enums.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskUpdateRequest(
        @Size(min = 2, max = 200) String title,
        @Size(max = 5000) String description,
        TaskStatus status,
        TaskPriority priority,
        Long assigneeId,
        LocalDateTime dueDate
) {}
