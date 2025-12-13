package hr.fer.ecommerce.gateway.client.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private List<OrderItem> orderItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private Long productId;
        private Integer quantity;
    }
}

