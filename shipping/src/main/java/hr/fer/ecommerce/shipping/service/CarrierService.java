package hr.fer.ecommerce.shipping.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@Getter
@Setter
public class CarrierService {

    private final List<String> supportedCarriers = Arrays.asList(
        "DHL", "DPD", "GLS", "Hrvatska Pošta"
    );

    private boolean carrierAvailabilityEnabled = true;
    private boolean carrierCapacityEnabled = true;

    public boolean validateCarrierAvailability(String carrier) {
        log.info("Validating carrier availability: {}", carrier);

        if (!supportedCarriers.contains(carrier)) {
            log.warn("Unsupported carrier: {}", carrier);
            return false;
        }

        if (!carrierAvailabilityEnabled) {
            log.warn("Carrier {} is currently unavailable (validation disabled)", carrier);
            return false;
        }

        log.info("Carrier {} is available", carrier);
        return true;
    }

    public boolean validateShippingAddress(String carrier, String address) {
        log.info("Validating shipping address for carrier {}", carrier);

        if (address == null || address.trim().isEmpty()) {
            log.warn("Invalid shipping address");
            return false;
        }

        if (address.length() < 10) {
            log.warn("Shipping address too short");
            return false;
        }

        log.info("Shipping address validation passed");
        return true;
    }

    public boolean checkCarrierCapacity(String carrier) {
        log.info("Checking carrier capacity: {}", carrier);

        if (!carrierCapacityEnabled) {
            log.warn("Carrier {} is at full capacity (validation disabled)", carrier);
            return false;
        }

        log.info("Carrier {} has capacity", carrier);
        return true;
    }
}
