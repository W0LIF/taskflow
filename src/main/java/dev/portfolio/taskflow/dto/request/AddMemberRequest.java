package dev.portfolio.taskflow.dto.request;

import dev.portfolio.taskflow.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull Long userId,
        @NotNull ProjectRole projectRole
) {}
