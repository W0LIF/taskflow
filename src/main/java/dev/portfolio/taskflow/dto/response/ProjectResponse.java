package dev.portfolio.taskflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserSummaryResponse owner,
        List<ProjectMemberResponse> members,
        long taskCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}