package dev.portfolio.taskflow.dto.request;

import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
        @Size(min = 2, max = 150) String name,
        @Size(max = 5000) String description
) {}
