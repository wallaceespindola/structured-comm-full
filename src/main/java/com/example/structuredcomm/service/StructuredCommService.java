package com.example.structuredcomm.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.regex.Pattern;

@Service
public class StructuredCommService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final Pattern STRUCTURED_PATTERN =
            Pattern.compile("^\\+\\+\\+\\d{3}/\\d{4}/\\d{5}\\+\\+\\+$");
    private static final Pattern NUMERIC12 = Pattern.compile("^\\d{12}$");

    public record Result(String structured, String numeric, boolean valid, String reason, Instant timestamp) {}

    public Result generate() {
        String base10 = "%010d".formatted(RNG.nextLong(0, 1_000_000_0000L));
        String check = computeCheck(base10);
        String numeric = base10 + check;
        String structured = toStructured(numeric);
        return new Result(structured, numeric, true, null, Instant.now());
    }

    public Result validateStructured(String input) {
        Instant now = Instant.now();
        if (input == null || !STRUCTURED_PATTERN.matcher(input).matches()) {
            return new Result(null, null, false, "Format must be +++XXX/XXXX/XXXXX+++", now);
        }
        String numeric = digitsOnly(input);
        return validateNumericInternal(numeric, now);
    }

    public Result validateNumeric(String numeric) {
        return validateNumericInternal(numeric, Instant.now());
    }

    private Result validateNumericInternal(String numeric, Instant ts) {
        if (numeric == null || !NUMERIC12.matcher(numeric).matches()) {
            return new Result(null, null, false, "Numeric value must be exactly 12 digits", ts);
        }
        String base10 = numeric.substring(0, 10);
        String givenCheck = numeric.substring(10, 12);
        String expectedCheck = computeCheck(base10);

        boolean ok = expectedCheck.equals(givenCheck);
        String reason = ok ? null : "Invalid check digits: expected " + expectedCheck + " for base " + base10;

        return new Result(toStructured(numeric), numeric, ok, reason, ts);
    }

    private String computeCheck(String base10) {
        long n = Long.parseLong(base10);
        long mod = n % 97L;
        long check = 97L - mod;
        if (check == 0L) check = 97L;
        return "%02d".formatted(check);
    }

    private String toStructured(String numeric12) {
        String p1 = numeric12.substring(0, 3);
        String p2 = numeric12.substring(3, 7);
        String p3 = numeric12.substring(7, 12);
        return "+++" + p1 + "/" + p2 + "/" + p3 + "+++";
    }

    private String digitsOnly(String s) {
        return s.replaceAll("\\D", "");
    }
}
