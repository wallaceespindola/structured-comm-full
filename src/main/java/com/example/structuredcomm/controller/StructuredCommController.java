package com.example.structuredcomm.controller;

import com.example.structuredcomm.dto.ValidateRequest;
import com.example.structuredcomm.dto.ValidationResponse;
import com.example.structuredcomm.service.StructuredCommService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path = "/api/comm", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Structured Communication", description = "Generate & validate Belgian structured communications")
public class StructuredCommController {

    private final StructuredCommService service;

    public StructuredCommController(StructuredCommService service) {
        this.service = service;
    }

    @GetMapping("/generate")
    @Operation(summary = "Generate a random valid structured communication")
    public ValidationResponse generate() {
        var r = service.generate();
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @PostMapping("/validate/structured")
    @Operation(summary = "Validate a fully structured value (POST body)", description = "Format: +++XXX/XXXX/XXXXX+++")
    public ValidationResponse validateStructured(@Valid @RequestBody ValidateRequest req) {
        var r = service.validateStructured(req.value());
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @PostMapping("/validate/numeric")
    @Operation(summary = "Validate a numeric-only 12-digit value (POST body)")
    public ValidationResponse validateNumeric(@Valid @RequestBody ValidateRequest req) {
        var r = service.validateNumeric(req.value());
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @GetMapping("/validate/structured")
    @Operation(summary = "Validate a fully structured value (GET query param)", description = "Format: +++XXX/XXXX/XXXXX+++; use ?value=...")
    public ValidationResponse validateStructuredGet(@RequestParam("value") @NotBlank String value,
                                                    HttpServletRequest request) {
        var v = normalizeQuery(value);
        var r = service.validateStructured(v);
        if (!r.valid()) {
            // Retry with spaces converted to '+' in case '+' was decoded to space
            var retry = service.validateStructured(v.replace(' ', '+'));
            if (retry.valid()) r = retry;
            else {
                // Try reading raw query string to preserve '+' characters
                String raw = rawQueryParam(request, "value");
                if (raw != null) {
                    var r2 = service.validateStructured(raw);
                    if (r2.valid()) r = r2;
                    else {
                        var r3 = service.validateStructured(raw.replace(' ', '+'));
                        if (r3.valid()) r = r3;
                    }
                }
            }
        }
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @GetMapping("/validate/numeric/{value}")
    @Operation(summary = "Validate a numeric-only 12-digit value (GET path variable)", description = "Use /validate/numeric/{value}")
    public ValidationResponse validateNumericGet(@PathVariable("value") @NotBlank String value) {
        var r = service.validateNumeric(value);
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    // Identify in-line: structured VCS
    @PostMapping("/identify/structured")
    @Operation(summary = "Identify & validate a structured VCS in a free-form line (POST body)",
            description = "Finds +++XXX/XXXX/XXXXX+++ within the given line and validates it")
    public ValidationResponse identifyStructuredInLinePost(@Valid @RequestBody ValidateRequest req) {
        var r = service.identifyStructuredInLine(req.value());
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @GetMapping("/identify/structured")
    @Operation(summary = "Identify & validate a structured VCS in a free-form line (GET query param)",
            description = "Use ?value=... with a full line of text containing +++XXX/XXXX/XXXXX+++")
    public ValidationResponse identifyStructuredInLineGet(@RequestParam("value") @NotBlank String value,
                                                          HttpServletRequest request) {
        var v = normalizeQuery(value);
        var r = service.identifyStructuredInLine(v);
        if (!r.valid()) {
            // A second chance if '+' became spaces in the embedded structured token
            var retry = service.identifyStructuredInLine(v.replace(" +++", " +++").replace(' ', '+'));
            if (retry.valid()) r = retry;
            else {
                String raw = rawQueryParam(request, "value");
                if (raw != null) {
                    var r2 = service.identifyStructuredInLine(raw);
                    if (r2.valid()) r = r2;
                    else {
                        var r3 = service.identifyStructuredInLine(raw.replace(' ', '+'));
                        if (r3.valid()) r = r3;
                    }
                }
            }
        }
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    // Identify in-line: numeric 12-digit VCS
    @PostMapping("/identify/numeric")
    @Operation(summary = "Identify & validate a numeric 12-digit VCS in a free-form line (POST body)",
            description = "Finds a 12-digit sequence within the given line and validates it")
    public ValidationResponse identifyNumericInLinePost(@Valid @RequestBody ValidateRequest req) {
        var r = service.identifyNumericInLine(req.value());
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @GetMapping("/identify/numeric")
    @Operation(summary = "Identify & validate a numeric 12-digit VCS in a free-form line (GET query param)",
            description = "Use ?value=... with a full line of text containing a 12-digit VCS")
    public ValidationResponse identifyNumericInLineGet(@RequestParam("value") @NotBlank String value,
                                                       HttpServletRequest request) {
        var v = normalizeQuery(value);
        var r = service.identifyNumericInLine(v);
        if (!r.valid()) {
            String raw = rawQueryParam(request, "value");
            if (raw != null) {
                var r2 = service.identifyNumericInLine(raw);
                if (r2.valid()) r = r2;
            }
        }
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    // --- helpers ---
    private String normalizeQuery(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        // strip wrapping quotes if present
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String rawQueryParam(HttpServletRequest req, String name) {
        if (req == null) return null;
        String qs = req.getQueryString();
        if (qs == null) return null;
        for (String part : qs.split("&")) {
            int i = part.indexOf('=');
            String key = i >= 0 ? part.substring(0, i) : part;
            if (name.equals(key)) {
                String val = i >= 0 ? part.substring(i + 1) : "";
                // Decode %XX but do NOT convert '+' to space
                return UriUtils.decode(val, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
