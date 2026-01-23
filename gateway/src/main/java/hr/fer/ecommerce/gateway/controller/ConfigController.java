package hr.fer.ecommerce.gateway.controller;

import hr.fer.ecommerce.gateway.client.MicroserviceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway/config")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final MicroserviceClient microserviceClient;

    @PostMapping("/fina/availability/{enabled}")
    public ResponseEntity<Map<String, Object>> setFinaAvailability(@PathVariable boolean enabled) {
        log.info("Gateway: Setting FINA availability to {}", enabled);
        var response = microserviceClient.setFinaAvailability(enabled);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fina/pre-authorization/{enabled}")
    public ResponseEntity<Map<String, Object>> setPreAuthorization(@PathVariable boolean enabled) {
        log.info("Gateway: Setting pre-authorization to {}", enabled);
        var response = microserviceClient.setPreAuthorization(enabled);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fina/status")
    public ResponseEntity<Map<String, Object>> getFinaStatus() {
        log.info("Gateway: Getting FINA status");
        var status = microserviceClient.getFinaStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/carrier/availability/{enabled}")
    public ResponseEntity<Map<String, Object>> setCarrierAvailability(@PathVariable boolean enabled) {
        log.info("Gateway: Setting carrier availability to {}", enabled);
        var response = microserviceClient.setCarrierAvailability(enabled);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/carrier/capacity/{enabled}")
    public ResponseEntity<Map<String, Object>> setCarrierCapacity(@PathVariable boolean enabled) {
        log.info("Gateway: Setting carrier capacity to {}", enabled);
        var response = microserviceClient.setCarrierCapacity(enabled);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/carrier/status")
    public ResponseEntity<Map<String, Object>> getCarrierStatus() {
        log.info("Gateway: Getting carrier status");
        var status = microserviceClient.getCarrierStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllStatus() {
        log.info("Gateway: Getting all configuration status");
        Map<String, Object> allStatus = new HashMap<>();

        try {
            allStatus.put("fina", microserviceClient.getFinaStatus());
        } catch (Exception e) {
            log.error("Failed to get FINA status", e);
            allStatus.put("fina", Map.of("error", "Failed to fetch"));
        }

        try {
            allStatus.put("carrier", microserviceClient.getCarrierStatus());
        } catch (Exception e) {
            log.error("Failed to get carrier status", e);
            allStatus.put("carrier", Map.of("error", "Failed to fetch"));
        }

        return ResponseEntity.ok(allStatus);
    }
}
