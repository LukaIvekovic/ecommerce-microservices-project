package hr.fer.ecommerce.payment.dto;

import hr.fer.ecommerce.payment.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateDto {
    @NotNull
    private PaymentStatus status;

    private String failureReason;
}

