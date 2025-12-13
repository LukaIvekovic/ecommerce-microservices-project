package hr.fer.ecommerce.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequestDto {
    @NotNull
    private Long orderId;

    @NotBlank
    private String carrier;

    private LocalDateTime estimatedDeliveryDate;
}

