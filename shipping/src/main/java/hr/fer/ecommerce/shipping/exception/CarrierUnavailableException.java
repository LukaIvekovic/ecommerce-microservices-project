package hr.fer.ecommerce.shipping.exception;

public class CarrierUnavailableException extends RuntimeException {
    public CarrierUnavailableException(String carrier) {
        super(String.format("Carrier '%s' is currently unavailable", carrier));
    }

    public CarrierUnavailableException(String carrier, String reason) {
        super(String.format("Carrier '%s' is currently unavailable: %s", carrier, reason));
    }
}
