package hr.fer.ecommerce.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    @NotBlank
    private String shippingAddress;

    @NotEmpty
    private List<OrderItemRequestDto> orderItems;
}

