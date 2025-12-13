package hr.fer.ecommerce.gateway.service;

import hr.fer.ecommerce.gateway.client.MicroserviceClient;
import hr.fer.ecommerce.gateway.dto.PlaceOrderRequest;
import hr.fer.ecommerce.gateway.dto.PlaceOrderResponse;
import hr.fer.ecommerce.gateway.client.order.CreateOrderRequest;
import hr.fer.ecommerce.gateway.client.order.OrderResponse;
import hr.fer.ecommerce.gateway.client.payment.CreatePaymentRequest;
import hr.fer.ecommerce.gateway.client.payment.PaymentResponse;
import hr.fer.ecommerce.gateway.client.shipment.CreateShipmentRequest;
import hr.fer.ecommerce.gateway.client.shipment.ShipmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {

    private final MicroserviceClient microserviceClient;

    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        log.info("Starting order placement for customer: {}", request.getCustomerEmail());

        try {
            log.info("Step 1: Creating order...");
            OrderResponse order = createOrder(request);
            log.info("Order created with ID: {}", order.getId());

            log.info("Step 2: Processing payment...");
            PaymentResponse payment = createPayment(request, order);
            log.info("Payment created with ID: {} and transaction ID: {}", payment.getId(), payment.getTransactionId());

            log.info("Step 3: Creating shipment...");
            ShipmentResponse shipment = createShipment(request, order);
            log.info("Shipment created with ID: {}", shipment.getId());

            PlaceOrderResponse response = PlaceOrderResponse.builder()
                    .success(true)
                    .message("Order placed successfully")
                    .orderId(order.getId())
                    .orderStatus(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .transactionId(payment.getTransactionId())
                    .shipmentId(shipment.getId())
                    .shipmentStatus(shipment.getStatus())
                    .trackingNumber(shipment.getTrackingNumber())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Order placement completed successfully for order ID: {}", order.getId());
            return response;

        } catch (Exception e) {
            log.error("Error during order placement: {}", e.getMessage(), e);

            return PlaceOrderResponse.builder()
                    .success(false)
                    .message("Failed to place order")
                    .errorDetails(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private OrderResponse createOrder(PlaceOrderRequest request) {
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .orderItems(request.getOrderItems().stream()
                        .map(item -> CreateOrderRequest.OrderItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        return microserviceClient.createOrder(orderRequest);
    }

    private PaymentResponse createPayment(PlaceOrderRequest request, OrderResponse order) {
        CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                .orderId(order.getId())
                .paidCustomerName(request.getCustomerName())
                .paidCustomerEmail(request.getCustomerEmail())
                .paymentMethod(request.getPaymentMethod())
                .paymentProvider(request.getPaymentProvider())
                .cardLastFourDigits(request.getCardLastFourDigits())
                .build();

        return microserviceClient.createPayment(paymentRequest);
    }

    private ShipmentResponse createShipment(PlaceOrderRequest request, OrderResponse order) {
        CreateShipmentRequest shipmentRequest = CreateShipmentRequest.builder()
                .orderId(order.getId())
                .carrier(request.getCarrier())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(7))
                .build();

        return microserviceClient.createShipment(shipmentRequest);
    }
}

