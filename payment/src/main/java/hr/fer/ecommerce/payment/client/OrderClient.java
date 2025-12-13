package hr.fer.ecommerce.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderClient {

    private final RestTemplate restTemplate;

    @Value("${order.service.url:http://localhost:8082}")
    private String orderServiceUrl;

    public OrderDto getOrder(Long orderId) {
        String url = orderServiceUrl + "/api/orders/" + orderId;
        log.info("Fetching order from: {}", url);
        return restTemplate.getForObject(url, OrderDto.class);
    }
}

