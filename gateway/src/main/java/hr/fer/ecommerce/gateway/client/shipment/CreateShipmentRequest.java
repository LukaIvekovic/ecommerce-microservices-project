package hr.fer.ecommerce.gateway.client.shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShipmentRequest {
    private Long orderId;
    private String carrier;
    private LocalDateTime estimatedDeliveryDate;
}

