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
public class ShipmentResponse {
    private Long id;
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String carrier;
    private String trackingNumber;
    private String status;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

