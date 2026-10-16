package dev.portfolio.taskflow.dto.request;

import dev.portfolio.taskflow.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public record TaskCreateRequest(
        @NotBlank @Size(min = 2, max = 200) String title,
        @Size(max = 5000) String description,
        TaskPriority priority,
        Long assigneeId,
        LocalDateTime dueDate,
        Set<Long> labelIds
) {}
