package hr.fer.ecommerce.shipping.exception;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(Long id) {
        super("Shipment not found with id: " + id);
    }

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}

