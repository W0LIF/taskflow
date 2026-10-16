package dev.portfolio.taskflow.dto.response;

import dev.portfolio.taskflow.enums.ProjectRole;

import java.time.LocalDateTime;

public record ProjectMemberResponse(
        Long id,
        UserSummaryResponse user,
        ProjectRole projectRole,
        LocalDateTime joinedAt
) {}