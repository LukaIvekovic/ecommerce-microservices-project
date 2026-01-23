package hr.fer.ecommerce.payment.controller;

import hr.fer.ecommerce.payment.service.FinancialAgencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config/fina")
@RequiredArgsConstructor
@Slf4j
public class FinancialAgencyConfigController {

    private final FinancialAgencyService financialAgencyService;

    @PostMapping("/availability/{enabled}")
    public ResponseEntity<Map<String, Object>> setFinaAvailability(@PathVariable boolean enabled) {
        financialAgencyService.setFinaAvailabilityEnabled(enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("setting", "finaAvailability");
        response.put("enabled", enabled);
        response.put("message", enabled ?
            "FINA availability validation will PASS" :
            "FINA availability validation will FAIL");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pre-authorization/{enabled}")
    public ResponseEntity<Map<String, Object>> setPreAuthorization(@PathVariable boolean enabled) {
        financialAgencyService.setPreAuthorizationEnabled(enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("setting", "preAuthorization");
        response.put("enabled", enabled);
        response.put("message", enabled ?
            "Payment pre-authorization will PASS" :
            "Payment pre-authorization will FAIL");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("finaAvailabilityEnabled", financialAgencyService.isFinaAvailabilityEnabled());
        status.put("preAuthorizationEnabled", financialAgencyService.isPreAuthorizationEnabled());

        return ResponseEntity.ok(status);
    }
}
