package hr.fer.ecommerce.order.dto;

import hr.fer.ecommerce.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

