package hr.fer.ecommerce.shipping.exception;

public class DuplicateShipmentException extends RuntimeException {
    public DuplicateShipmentException(Long orderId) {
        super(String.format("Shipment already exists for order: %d", orderId));
    }
}
