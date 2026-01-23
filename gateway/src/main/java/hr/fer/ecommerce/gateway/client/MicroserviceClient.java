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


    // ===== 2PC Methods =====
    public OrderResponse prepareOrder(CreateOrderRequest request) {
        String url = servicesConfig.getOrder().getUrl() + "/api/orders/prepare";
        log.info("2PC: Preparing order at: {}", url);

        try {
            OrderResponse response = restTemplate.postForObject(url, request, OrderResponse.class);
            log.info("2PC: Order prepared with ID: {}", response != null ? response.getId() : "null");
            return response;
        } catch (Exception e) {
            log.error("2PC: Failed to prepare order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare order: " + e.getMessage(), e);
        }
    }

    public void commitOrder(Long orderId) {
        String url = servicesConfig.getOrder().getUrl() + "/api/orders/" + orderId + "/commit";
        log.info("2PC: Committing order at: {}", url);

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("2PC: Order committed - ID: {}", orderId);
        } catch (Exception e) {
            log.error("2PC: Failed to commit order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to commit order: " + e.getMessage(), e);
        }
    }

    public void abortOrder(Long orderId) {
        String url = servicesConfig.getOrder().getUrl() + "/api/orders/" + orderId + "/abort";
        log.info("2PC: Aborting order at: {}", url);

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("2PC: Order aborted - ID: {}", orderId);
        } catch (Exception e) {
            log.error("2PC: Failed to abort order {}: {}", orderId, e.getMessage(), e);
        }
    }

    public PaymentResponse preparePayment(CreatePaymentRequest request) {
        String url = servicesConfig.getPayment().getUrl() + "/api/payments/prepare";
        log.info("2PC: Preparing payment at: {}", url);

        try {
            PaymentResponse response = restTemplate.postForObject(url, request, PaymentResponse.class);
            log.info("2PC: Payment prepared with ID: {}", response != null ? response.getId() : "null");
            return response;
        } catch (Exception e) {
            log.error("2PC: Failed to prepare payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare payment: " + e.getMessage(), e);
        }
    }

    public void commitPayment(Long paymentId) {
        String url = servicesConfig.getPayment().getUrl() + "/api/payments/" + paymentId + "/commit";
        log.info("2PC: Committing payment at: {}", url);

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("2PC: Payment committed - ID: {}", paymentId);
        } catch (Exception e) {
            log.error("2PC: Failed to commit payment {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Failed to commit payment: " + e.getMessage(), e);
        }
    }

    public void abortPayment(Long paymentId) {
        String url = servicesConfig.getPayment().getUrl() + "/api/payments/" + paymentId + "/abort";
        log.info("2PC: Aborting payment at: {}", url);

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("2PC: Payment aborted - ID: {}", paymentId);
        } catch (Exception e) {
            log.error("2PC: Failed to abort payment {}: {}", paymentId, e.getMessage(), e);
        }
    }

    public ShipmentResponse prepareShipment(CreateShipmentRequest request) {
        String url = servicesConfig.getShipping().getUrl() + "/api/shipments/prepare";
        log.info("2PC: Preparing shipment at: {}", url);

        try {
            ShipmentResponse response = restTemplate.postForObject(url, request, ShipmentResponse.class);
            log.info("2PC: Shipment prepared with ID: {}", response != null ? response.getId() : "null");
            return response;
        } catch (Exception e) {
            log.error("2PC: Failed to prepare shipment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to prepare shipment: " + e.getMessage(), e);
        }
    }

    public void commitShipment(Long shipmentId) {
        String url = servicesConfig.getShipping().getUrl() + "/api/shipments/" + shipmentId + "/commit";
        log.info("2PC: Committing shipment at: {}", url);

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("2PC: Shipment committed - ID: {}", shipmentId);
        } catch (Exception e) {
            log.error("2PC: Failed to commit shipment {}: {}", shipmentId, e.getMessage(), e);
            throw new RuntimeException("Failed to commit shipment: " + e.getMessage(), e);
        }
    }

    public void abortShipment(Long shipmentId) {
        String url = servicesConfig.getShipping().getUrl() + "/api/shipments/" + shipmentId + "/abort";
        log.info("2PC: Aborting shipment at: {}", url);

        try {
            restTemplate.postForObject(url, null, Void.class);
            log.info("2PC: Shipment aborted - ID: {}", shipmentId);
        } catch (Exception e) {
            log.error("2PC: Failed to abort shipment {}: {}", shipmentId, e.getMessage(), e);
        }
    }
}

