package hr.fer.ecommerce.shipping.mapper;

import hr.fer.ecommerce.shipping.dto.ShipmentDto;
import hr.fer.ecommerce.shipping.model.Shipment;

public class ShipmentMapper {

    public static ShipmentDto toDTO(Shipment shipment) {
        return ShipmentDto.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .customerName(shipment.getCustomerName())
                .customerEmail(shipment.getCustomerEmail())
                .shippingAddress(shipment.getShippingAddress())
                .carrier(shipment.getCarrier())
                .trackingNumber(shipment.getTrackingNumber())
                .status(shipment.getStatus())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .actualDeliveryDate(shipment.getActualDeliveryDate())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}

