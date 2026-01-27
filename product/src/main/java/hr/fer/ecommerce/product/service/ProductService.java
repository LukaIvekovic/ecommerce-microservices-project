package hr.fer.ecommerce.product.service;

import hr.fer.ecommerce.product.dto.ProductDto;
import hr.fer.ecommerce.product.dto.ProductRequestDto;
import hr.fer.ecommerce.product.dto.StockReservationRequest;
import hr.fer.ecommerce.product.dto.StockValidationRequest;
import hr.fer.ecommerce.product.exception.InsufficientStockException;
import hr.fer.ecommerce.product.exception.ProductNotFoundException;
import hr.fer.ecommerce.product.mapper.ProductMapper;
import hr.fer.ecommerce.product.model.Product;
import hr.fer.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductMapper.toDTO(product);
    }

    @Transactional
    public ProductDto createProduct(ProductRequestDto request) {
        Product product = ProductMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        log.info("Created product: {}", savedProduct.getId());
        return ProductMapper.toDTO(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        ProductMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);

        log.info("Updated product: {}", id);
        return ProductMapper.toDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product: {}", id);
    }

    @Transactional(readOnly = true)
    public void validateStockAvailability(StockReservationRequest request) {
        long startTime = System.nanoTime();
        log.info("Validating stock availability for {} items", request.getItems().size());

        for (StockValidationRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                log.warn("Insufficient stock for product {}: requested={}, available={}",
                        product.getId(), item.getQuantity(), product.getStockQuantity());
                throw new InsufficientStockException(product.getId(), item.getQuantity(), product.getStockQuantity());
            }
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Stock validation for {} items passed in {} ms", request.getItems().size(), duration);
    }


    @Transactional
    public void reserveStock(StockReservationRequest request) {
        log.info("Reserving stock for {} items", request.getItems().size());

        for (StockValidationRequest item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                log.error("Insufficient stock during reservation for product {}: requested={}, available={}",
                    product.getId(), item.getQuantity(), product.getStockQuantity());
                throw new InsufficientStockException(
                    product.getId(),
                    item.getQuantity(),
                    product.getStockQuantity()
                );
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            log.info("Reserved {} units of product {}, remaining stock: {}",
                item.getQuantity(), product.getId(), product.getStockQuantity());
        }

        log.info("Stock reservation completed successfully");
    }

    @Transactional
    public void releaseStock(StockReservationRequest request) {
        log.info("Releasing stock for {} items (rollback)", request.getItems().size());

        for (StockValidationRequest item : request.getItems()) {
            try {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);

                log.info("Released {} units of product {}, new stock: {}",
                    item.getQuantity(), product.getId(), product.getStockQuantity());
            } catch (Exception e) {
                log.error("Failed to release stock for product {}: {}",
                    item.getProductId(), e.getMessage(), e);
            }
        }

        log.info("Stock release completed");
    }
}

