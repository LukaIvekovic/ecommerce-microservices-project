package hr.fer.ecommerce.shipping.controller;

import hr.fer.ecommerce.shipping.service.CarrierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config/carrier")
@RequiredArgsConstructor
@Slf4j
public class CarrierConfigController {

    private final CarrierService carrierService;

    @PostMapping("/availability/{enabled}")
    public ResponseEntity<Map<String, Object>> setCarrierAvailability(@PathVariable boolean enabled) {
        carrierService.setCarrierAvailabilityEnabled(enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("setting", "carrierAvailability");
        response.put("enabled", enabled);
        response.put("message", enabled ?
            "Carrier availability validation will PASS" :
            "Carrier availability validation will FAIL");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/capacity/{enabled}")
    public ResponseEntity<Map<String, Object>> setCarrierCapacity(@PathVariable boolean enabled) {
        carrierService.setCarrierCapacityEnabled(enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("setting", "carrierCapacity");
        response.put("enabled", enabled);
        response.put("message", enabled ?
            "Carrier capacity validation will PASS" :
            "Carrier capacity validation will FAIL");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("carrierAvailabilityEnabled", carrierService.isCarrierAvailabilityEnabled());
        status.put("carrierCapacityEnabled", carrierService.isCarrierCapacityEnabled());

        return ResponseEntity.ok(status);
    }
}
