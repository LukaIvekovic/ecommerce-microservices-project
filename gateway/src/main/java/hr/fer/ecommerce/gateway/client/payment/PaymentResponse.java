package hr.fer.ecommerce.gateway.client.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private String paymentProvider;
    private String cardLastFourDigits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

