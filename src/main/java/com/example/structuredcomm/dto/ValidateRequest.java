package com.example.structuredcomm.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateRequest(@NotBlank String value) {}
