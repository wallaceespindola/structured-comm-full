package com.example.structuredcomm.dto;

import java.time.Instant;

public record ValidationResponse(
        String structured,
        String numeric,
        boolean valid,
        String reason,
        Instant timestamp
) {}
