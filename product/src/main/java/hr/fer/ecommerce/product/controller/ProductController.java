package hr.fer.ecommerce.product.controller;

import hr.fer.ecommerce.product.dto.ProductDto;
import hr.fer.ecommerce.product.dto.ProductRequestDto;
import hr.fer.ecommerce.product.dto.StockReservationRequest;
import hr.fer.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductRequestDto request) {
        ProductDto product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequestDto request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stock/validate")
    public ResponseEntity<Void> validateStock(@RequestBody @Valid StockReservationRequest request) {
        productService.validateStockAvailability(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock/reserve")
    public ResponseEntity<Void> reserveStock(@RequestBody @Valid StockReservationRequest request) {
        productService.reserveStock(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock/release")
    public ResponseEntity<Void> releaseStock(@RequestBody @Valid StockReservationRequest request) {
        productService.releaseStock(request);
        return ResponseEntity.ok().build();
    }
}

