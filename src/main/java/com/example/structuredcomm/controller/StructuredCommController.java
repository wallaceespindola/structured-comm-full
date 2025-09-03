package com.example.structuredcomm.controller;

import com.example.structuredcomm.dto.ValidateRequest;
import com.example.structuredcomm.dto.ValidationResponse;
import com.example.structuredcomm.service.StructuredCommService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public ValidationResponse validateStructuredGet(@RequestParam @NotBlank String value) {
        var r = service.validateStructured(value);
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }

    @GetMapping("/validate/numeric")
    @Operation(summary = "Validate a numeric-only 12-digit value (GET query param)", description = "Use ?value=...")
    public ValidationResponse validateNumericGet(@RequestParam @NotBlank String value) {
        var r = service.validateNumeric(value);
        return new ValidationResponse(r.structured(), r.numeric(), r.valid(), r.reason(), r.timestamp());
    }
}
