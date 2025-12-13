package hr.fer.ecommerce.payment.dto;

import hr.fer.ecommerce.payment.model.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    @NotNull
    private Long orderId;

    @NotBlank
    private String paidCustomerName;

    @Email
    @NotBlank
    private String paidCustomerEmail;

    @NotNull
    private PaymentMethod paymentMethod;

    private String cardLastFourDigits;

    private String paymentProvider;
}

