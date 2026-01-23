package hr.fer.ecommerce.payment.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(Long orderId) {
        super(String.format("Payment already exists for order: %d", orderId));
    }
}
