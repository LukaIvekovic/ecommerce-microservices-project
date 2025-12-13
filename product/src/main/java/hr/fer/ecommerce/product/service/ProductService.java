package hr.fer.ecommerce.product.service;

import hr.fer.ecommerce.product.dto.ProductDto;
import hr.fer.ecommerce.product.dto.ProductRequestDto;
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
}

