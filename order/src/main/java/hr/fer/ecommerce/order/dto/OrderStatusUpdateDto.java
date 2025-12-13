package hr.fer.ecommerce.order.dto;

import hr.fer.ecommerce.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateDto {
    @NotNull
    private OrderStatus status;
}

