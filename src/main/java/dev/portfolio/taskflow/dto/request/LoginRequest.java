package dev.portfolio.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank String username,
    @NotBlank String password
){}
