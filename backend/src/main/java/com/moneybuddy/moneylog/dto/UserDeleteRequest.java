package com.moneybuddy.moneylog.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(
        @NotBlank String password
) {}