package hr.fer.ecommerce.payment.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Setter
@Getter
public class FinancialAgencyService {

    private boolean finaAvailabilityEnabled = true;
    private boolean preAuthorizationEnabled = true;

    public boolean validateFinaAvailability() {
        log.info("Validating FINA service availability");

        if (!finaAvailabilityEnabled) {
            log.warn("FINA service is currently unavailable (validation disabled)");
            return false;
        }

        log.info("FINA service is available");
        return true;
    }

    public boolean validatePaymentMethod(String paymentMethod, String cardLastFourDigits) {
        log.info("Validating payment method: {}", paymentMethod);

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return false;
        }

        if (paymentMethod.equalsIgnoreCase("CREDIT_CARD") || paymentMethod.equalsIgnoreCase("DEBIT_CARD")) {
            if (cardLastFourDigits == null || !cardLastFourDigits.matches("\\d{4}")) {
                log.warn("Invalid card format");
                return false;
            }
        }

        log.info("Payment method validation passed");
        return true;
    }

    public boolean preAuthorizePayment(String transactionId, java.math.BigDecimal amount) {
        log.info("Pre-authorizing payment {} for amount {}", transactionId, amount);

        if (!preAuthorizationEnabled) {
            log.warn("Payment pre-authorization failed (validation disabled)");
            return false;
        }

        log.info("Payment pre-authorization successful");
        return true;
    }
}
