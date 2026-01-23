package hr.fer.ecommerce.payment.model;

public enum PaymentStatus {
    PENDING,
    PRE_AUTHORIZED,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}

