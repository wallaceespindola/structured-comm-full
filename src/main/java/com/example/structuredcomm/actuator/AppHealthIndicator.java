package com.example.structuredcomm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class AppHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        return Health.up()
                .withDetail("timestamp", now.toString())
                .withDetail("epochMillis", Instant.now().toEpochMilli())
                .build();
    }
}
