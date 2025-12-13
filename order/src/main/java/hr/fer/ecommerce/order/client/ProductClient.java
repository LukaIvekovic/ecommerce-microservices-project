package hr.fer.ecommerce.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    public ProductDto getProduct(Long productId) {
        String url = productServiceUrl + "/api/products/" + productId;
        log.info("Fetching product from: {}", url);
        return restTemplate.getForObject(url, ProductDto.class);
    }
}

