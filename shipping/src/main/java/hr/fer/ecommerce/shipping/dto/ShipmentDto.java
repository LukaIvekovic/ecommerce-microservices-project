package hr.fer.ecommerce.shipping.dto;

import hr.fer.ecommerce.shipping.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDto {
    private Long id;
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String carrier;
    private String trackingNumber;
    private ShipmentStatus status;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

