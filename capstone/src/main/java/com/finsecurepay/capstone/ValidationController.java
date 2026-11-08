package com.finsecurepay.capstone;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "up",
            "service", "finsecurepay",
            "version", "cicd-ver2"
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validate(@RequestBody Transaction request) {

        ValidationResult result = validationService.validate(request);

        return result.valid() ? ResponseEntity.ok(result) : ResponseEntity.unprocessableEntity().body(result);
    }
}
