package dev.portfolio.taskflow.dto.response;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        String fullName
) {}
