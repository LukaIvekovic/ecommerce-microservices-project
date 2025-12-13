package hr.fer.ecommerce.gateway.client.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    private Long orderId;
    private String paidCustomerName;
    private String paidCustomerEmail;
    private String paymentMethod;
    private String paymentProvider;
    private String cardLastFourDigits;
}

