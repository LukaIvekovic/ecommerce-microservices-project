package hr.fer.ecommerce.order.mapper;

import hr.fer.ecommerce.order.dto.OrderDto;
import hr.fer.ecommerce.order.dto.OrderItemDto;
import hr.fer.ecommerce.order.model.Order;
import hr.fer.ecommerce.order.model.OrderItem;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDto toDTO(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderMapper::toItemDTO)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderItemDto toItemDTO(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}

