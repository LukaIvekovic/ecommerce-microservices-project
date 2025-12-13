package hr.fer.ecommerce.payment.mapper;

import hr.fer.ecommerce.payment.dto.PaymentDto;
import hr.fer.ecommerce.payment.model.Payment;

public class PaymentMapper {

    public static PaymentDto toDTO(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .customerName(payment.getPaidCustomerName())
                .customerEmail(payment.getPaidCustomerEmail())
                .amount(payment.getPaidAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentProvider(payment.getPaymentProvider())
                .cardLastFourDigits(payment.getCardLastFourDigits())
                .failureReason(payment.getFailureReason())
                .processedAt(payment.getProcessedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

