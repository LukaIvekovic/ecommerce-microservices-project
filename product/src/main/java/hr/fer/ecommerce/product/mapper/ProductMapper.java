package hr.fer.ecommerce.product.mapper;

import hr.fer.ecommerce.product.dto.ProductDto;
import hr.fer.ecommerce.product.dto.ProductRequestDto;
import hr.fer.ecommerce.product.model.Product;

public class ProductMapper {

    public static ProductDto toDTO(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static Product toEntity(ProductRequestDto request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
    }

    public static void updateEntity(Product product, ProductRequestDto request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
    }
}

