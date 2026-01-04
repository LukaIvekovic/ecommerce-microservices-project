package hr.fer.ecommerce.gateway.dto;

import hr.fer.ecommerce.gateway.client.order.OrderResponse;
import hr.fer.ecommerce.gateway.client.payment.PaymentResponse;
import hr.fer.ecommerce.gateway.client.shipment.ShipmentResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SagaContext {
    private OrderResponse order;
    private PaymentResponse payment;
    private ShipmentResponse shipment;
}
