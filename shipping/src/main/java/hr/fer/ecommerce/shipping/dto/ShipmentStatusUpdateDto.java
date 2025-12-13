package hr.fer.ecommerce.shipping.dto;

import hr.fer.ecommerce.shipping.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentStatusUpdateDto {
    @NotNull
    private ShipmentStatus status;
}

