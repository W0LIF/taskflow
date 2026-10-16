package dev.portfolio.taskflow.dto.response;

import java.time.LocalDateTime;

public record TaskHistoryResponse(
        Long id,
        UserSummaryResponse user,
        String fieldChanged,
        String oldValue,
        String newValue,
        LocalDateTime changedAt
) {}
