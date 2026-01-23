package hr.fer.ecommerce.gateway.client;

import hr.fer.ecommerce.gateway.client.order.CreateOrderRequest;
import hr.fer.ecommerce.gateway.client.order.OrderResponse;
import hr.fer.ecommerce.gateway.client.payment.CreatePaymentRequest;
import hr.fer.ecommerce.gateway.client.payment.PaymentResponse;
import hr.fer.ecommerce.gateway.client.shipment.CreateShipmentRequest;
import hr.fer.ecommerce.gateway.client.shipment.ShipmentResponse;
import hr.fer.ecommerce.gateway.config.ServicesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicroserviceClient {

    private final RestTemplate restTemplate;
    private final ServicesConfig servicesConfig;

    public OrderResponse createOrder(CreateOrderRequest request) {
        String url = servicesConfig.getOrder().getUrl() + "/api/orders";
        log.info("Creating order at: {}", url);

        try {
            OrderResponse response = restTemplate.postForObject(url, request, OrderResponse.class);
            log.info("Order created successfully with ID: {}", response != null ? response.getId() : "null");
            return response;
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        String url = servicesConfig.getPayment().getUrl() + "/api/payments";
        log.info("Creating payment at: {}", url);

        try {
            PaymentResponse response = restTemplate.postForObject(url, request, PaymentResponse.class);
            log.info("Payment created successfully with ID: {}", response != null ? response.getId() : "null");
            return response;
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        String url = servicesConfig.getShipping().getUrl() + "/api/shipments";
        log.info("Creating shipment at: {}", url);

        try {
            ShipmentResponse response = restTemplate.postForObject(url, request, ShipmentResponse.class);
            log.info("Shipment created successfully with ID: {}", response != null ? response.getId() : "null");
            return response;
        } catch (Exception e) {
            log.error("Error creating shipment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create shipment: " + e.getMessage(), e);
        }
    }

    public void cancelOrder(Long orderId) {
        String url = servicesConfig.getOrder().getUrl()
                + "/api/orders/" + orderId + "/cancel";

        log.warn("Compensating order: cancelling order ID {}", orderId);
        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            log.error("Failed to cancel order {}", orderId, e);
        }
    }

    public void refundPayment(Long paymentId) {
        String url = servicesConfig.getPayment().getUrl()
                + "/api/payments/" + paymentId + "/refund";

        log.warn("Compensating payment: refunding payment ID {}", paymentId);
        try {
            restTemplate.postForLocation(url, null);
        } catch (Exception e) {
            log.error("Failed to refund payment {}", paymentId, e);
        }
    }

    public void cancelShipment(Long shipmentId) {
        String url = servicesConfig.getShipping().getUrl()
                + "/api/shipments/" + shipmentId + "/cancel";

        log.warn("Compensating shipment: cancelling shipment ID {}", shipmentId);
        try {
            restTemplate.put(url, null);
        } catch (Exception e) {
            log.error("Failed to cancel shipment {}", shipmentId, e);
        }
    }

    public void validateStockAvailability(CreateOrderRequest request) {
        String url = servicesConfig.getProduct().getUrl() + "/api/products/stock/validate";
        log.info("Validating stock availability at: {}", url);

        try {
            restTemplate.postForObject(url, buildStockReservationRequest(request), Void.class);
            log.info("Stock validation passed");
        } catch (Exception e) {
            log.error("Stock validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Stock validation failed: " + e.getMessage(), e);
        }
    }

    public void reserveStock(CreateOrderRequest request) {
        String url = servicesConfig.getProduct().getUrl() + "/api/products/stock/reserve";
        log.info("Reserving stock at: {}", url);

        try {
            restTemplate.postForObject(url, buildStockReservationRequest(request), Void.class);
            log.info("Stock reserved successfully");
        } catch (Exception e) {
            log.error("Stock reservation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Stock reservation failed: " + e.getMessage(), e);
        }
    }

    public void releaseStock(CreateOrderRequest request) {
        String url = servicesConfig.getProduct().getUrl() + "/api/products/stock/release";
        log.info("Releasing stock at: {}", url);

        try {
            restTemplate.postForObject(url, buildStockReservationRequest(request), Void.class);
            log.info("Stock released successfully");
        } catch (Exception e) {
            log.error("Stock release failed: {}", e.getMessage(), e);
        }
    }

    public void validatePayment(CreatePaymentRequest request) {
        String url = servicesConfig.getPayment().getUrl() + "/api/payments/validate";
        log.info("Validating payment at: {}", url);

        try {
            restTemplate.postForObject(url, request, Void.class);
            log.info("Payment validation passed");
        } catch (Exception e) {
            log.error("Payment validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Payment validation failed: " + e.getMessage(), e);
        }
    }

    public void preAuthorizePayment(Long paymentId) {
        String url = servicesConfig.getPayment().getUrl() + "/api/payments/" + paymentId + "/pre-authorize";
        log.info("Pre-authorizing payment at: {}", url);

        try {
            restTemplate.postForObject(url, null, PaymentResponse.class);
            log.info("Payment pre-authorized successfully");
        } catch (Exception e) {
            log.error("Payment pre-authorization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Payment pre-authorization failed: " + e.getMessage(), e);
        }
    }

    public void validateShipment(CreateShipmentRequest request) {
        String url = servicesConfig.getShipping().getUrl() + "/api/shipments/validate";
        log.info("Validating shipment at: {}", url);

        try {
            restTemplate.postForObject(url, request, Void.class);
            log.info("Shipment validation passed");
        } catch (Exception e) {
            log.error("Shipment validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Shipment validation failed: " + e.getMessage(), e);
        }
    }

    private Object buildStockReservationRequest(CreateOrderRequest request) {
        return new Object() {
            public final java.util.List<Object> items = request.getOrderItems().stream()
                    .map(item -> new Object() {
                        public final Long productId = item.getProductId();
                        public final Integer quantity = item.getQuantity();
                    })
                    .collect(java.util.stream.Collectors.toList());
        };
    }

    private static class OrderStatusUpdateRequest {
        public final String status;
        public OrderStatusUpdateRequest(String status) {
            this.status = status;
        }
    }

    private static class PaymentStatusUpdateRequest {
        public final String status;
        public PaymentStatusUpdateRequest(String status) {
            this.status = status;
        }
    }

    private static class ShipmentStatusUpdateRequest {
        public final String status;
        public ShipmentStatusUpdateRequest(String status) {
            this.status = status;
        }
    }
}

