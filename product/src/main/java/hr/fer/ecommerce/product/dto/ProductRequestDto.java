package hr.fer.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;
}

